package be.monmoisclair.app

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import be.monmoisclair.app.data.widget.CarnetWidgetSink
import be.monmoisclair.app.domain.BiometricFlagCache
import be.monmoisclair.app.ui.MainViewModel
import be.monmoisclair.app.ui.lock.BiometricLockScreen
import be.monmoisclair.app.ui.navigation.AppNavHost
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.MonMoisClairTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var widgetSink: CarnetWidgetSink

    private var pendingHighlightExpenseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        pendingHighlightExpenseId = intent?.getStringExtra(EXTRA_HIGHLIGHT_EXPENSE_ID)
        val systemDarkBoot = (resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        val nightBoot = be.monmoisclair.app.domain.ThemePreferenceCache
            .peek(this)
            .resolveNight(systemDarkBoot)
        val barBoot = if (nightBoot) 0xFF1E1B18.toInt() else 0xFFF6F3E8.toInt()
        // 1er frame = Apparence choisie, pas la nuit du thème XML.
        window.setBackgroundDrawable(ColorDrawable(barBoot))
        if (BiometricFlagCache.peek(this)) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        enableEdgeToEdge(
            statusBarStyle = if (nightBoot) {
                SystemBarStyle.dark(barBoot)
            } else {
                SystemBarStyle.light(barBoot, barBoot)
            },
            navigationBarStyle = if (nightBoot) {
                SystemBarStyle.dark(barBoot)
            } else {
                SystemBarStyle.light(barBoot, barBoot)
            }
        )
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val state by mainViewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val night = state.themePreference.resolveNight(systemDark)
            MonMoisClairTheme(darkTheme = night) {
                val bar = if (night) 0xFF1E1B18.toInt() else 0xFFF6F3E8.toInt()
                DisposableEffect(night) {
                    enableEdgeToEdge(
                        statusBarStyle = if (night) {
                            SystemBarStyle.dark(bar)
                        } else {
                            SystemBarStyle.light(bar, bar)
                        },
                        navigationBarStyle = if (night) {
                            SystemBarStyle.dark(bar)
                        } else {
                            SystemBarStyle.light(bar, bar)
                        }
                    )
                    onDispose { }
                }
                // Widget « Comme le téléphone » : couleurs au bascule nuit système.
                LaunchedEffect(night) {
                    withContext(Dispatchers.IO) {
                        runCatching { widgetSink.refresh() }
                    }
                }
                val navController = rememberNavController()
                val lifecycleOwner = LocalLifecycleOwner.current

                var sessionEntered by rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(state.unlocked) {
                    if (state.unlocked) sessionEntered = true
                }

                // T3 : aperçu multitâche flouté / noir si biométrie activée.
                LaunchedEffect(state.biometricEnabled) {
                    if (state.biometricEnabled) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME ->
                                mainViewModel.reloadBiometricPreference()
                            Lifecycle.Event.ON_STOP -> {
                                if (!isChangingConfigurations) {
                                    mainViewModel.onAppBackgrounded()
                                }
                            }
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                // Zone libre boutons système : safeDrawing + tappable + bande confort (charte §6).
                // Une seule fois ici — les écrans ne doivent pas re-ajouter bottomSystemClearance.
                val rootModifier = Modifier
                    .fillMaxSize()
                    .background(A11yTokens.PaperCream)
                    .safeDrawingPadding()
                    .windowInsetsPadding(
                        WindowInsets.tappableElement.only(WindowInsetsSides.Bottom)
                    )
                    .padding(bottom = A11yTokens.bottomSystemClearance)

                when {
                    state.loading -> {
                        Box(
                            modifier = rootModifier.semantics {
                                contentDescription = "Chargement de Mon Mois Clair"
                                liveRegion = LiveRegionMode.Polite
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AppWordmark(width = 220.dp, announce = false)
                                CircularProgressIndicator(
                                    color = A11yTokens.Link,
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    text = "Chargement…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = A11yTokens.InkMuted
                                )
                            }
                        }
                    }
                    state.needsUnlock && !sessionEntered -> {
                        BiometricLockScreen(
                            gateway = mainViewModel.biometricAuth,
                            message = state.lockMessage,
                            onAuthenticated = mainViewModel::onUnlocked,
                            onAuthMessage = mainViewModel::setLockMessage,
                            modifier = rootModifier
                        )
                    }
                    else -> {
                        Box(modifier = rootModifier) {
                            AppNavHost(
                                navController = navController,
                                startDestination = state.startDestination,
                                modifier = if (state.needsUnlock) {
                                    // TalkBack ne doit pas atteindre FAIT sous le verrou.
                                    Modifier.fillMaxSize().clearAndSetSemantics { }
                                } else {
                                    Modifier.fillMaxSize()
                                }
                            )
                            if (state.needsUnlock) {
                                BiometricLockScreen(
                                    gateway = mainViewModel.biometricAuth,
                                    message = state.lockMessage,
                                    onAuthenticated = mainViewModel::onUnlocked,
                                    onAuthMessage = mainViewModel::setLockMessage,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingHighlightExpenseId = intent.getStringExtra(EXTRA_HIGHLIGHT_EXPENSE_ID)
    }

    /** Home consomme l’id une seule fois (deep link notif). */
    fun consumeHighlightExpenseId(): String? {
        val id = pendingHighlightExpenseId
        pendingHighlightExpenseId = null
        if (id != null) {
            intent?.removeExtra(EXTRA_HIGHLIGHT_EXPENSE_ID)
        }
        return id
    }

    companion object {
        const val EXTRA_HIGHLIGHT_EXPENSE_ID = "highlight_expense_id"
    }
}
