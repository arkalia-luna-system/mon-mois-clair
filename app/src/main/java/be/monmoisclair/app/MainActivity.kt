package be.monmoisclair.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import be.monmoisclair.app.ui.MainViewModel
import be.monmoisclair.app.ui.navigation.AppNavHost
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.MonMoisClairTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonMoisClairTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val state by mainViewModel.uiState.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                // safeDrawingPadding : évite status bar / gesture bar (S25 Ultra).
                val rootModifier = Modifier
                    .fillMaxSize()
                    .background(A11yTokens.PaperCream)
                    .safeDrawingPadding()

                if (state.loading) {
                    Box(
                        modifier = rootModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    AppNavHost(
                        navController = navController,
                        startDestination = state.startDestination,
                        modifier = rootModifier
                    )
                }
            }
        }
    }
}
