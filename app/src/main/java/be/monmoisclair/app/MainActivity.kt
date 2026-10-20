package be.monmoisclair.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import be.monmoisclair.app.ui.MainViewModel
import be.monmoisclair.app.ui.lock.BiometricLockScreen
import be.monmoisclair.app.ui.navigation.AppNavHost
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.MonMoisClairTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var pendingHighlightExpenseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingHighlightExpenseId = intent?.getStringExtra(EXTRA_HIGHLIGHT_EXPENSE_ID)
        enableEdgeToEdge()
        setContent {
            MonMoisClairTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val state by mainViewModel.uiState.collectAsStateWithLifecycle()
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
                            modifier = rootModifier,
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                                modifier = Modifier.fillMaxSize()
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
