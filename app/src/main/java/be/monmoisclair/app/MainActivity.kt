package be.monmoisclair.app

import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonMoisClairTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val state by mainViewModel.uiState.collectAsStateWithLifecycle()
                val navController = rememberNavController()
                val lifecycleOwner = LocalLifecycleOwner.current

                // Après le 1er déverrouillage de la session : garder NavHost vivant sous le
                // verrou (sinon ouvrir Batterie / SAF depuis Réglages perdait la pile).
                var sessionEntered by rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(state.unlocked) {
                    if (state.unlocked) sessionEntered = true
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

                // One UI : navigationBars souvent visible=false (geste / translucide)
                // → safeDrawing seul laisse 0 en bas. tappableElement = zone des 3 boutons.
                // + bottomSystemClearance = bande vide pour ne pas confondre app / système.
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
                    // Cold start verrouillé : pas de Home sous le lock (évite fuite visuelle).
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
}
