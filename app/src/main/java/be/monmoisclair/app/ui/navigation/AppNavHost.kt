package be.monmoisclair.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import be.monmoisclair.app.domain.usecase.StartDestination
import be.monmoisclair.app.ui.health.ReminderHealthScreen
import be.monmoisclair.app.ui.home.HomePlaceholderScreen
import be.monmoisclair.app.ui.onboarding.PermissionsSetupScreen
import be.monmoisclair.app.ui.onboarding.SafSetupScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: StartDestination,
    modifier: Modifier = Modifier
) {
    val start = when (startDestination) {
        StartDestination.SafSetup -> AppRoute.SafSetup
        StartDestination.PermissionsSetup -> AppRoute.PermissionsSetup
        StartDestination.Home -> AppRoute.Home
    }

    NavHost(
        navController = navController,
        startDestination = start,
        modifier = modifier
    ) {
        composable(AppRoute.SafSetup) {
            SafSetupScreen(
                onSafReady = {
                    navController.navigate(AppRoute.PermissionsSetup) {
                        popUpTo(AppRoute.SafSetup) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRoute.PermissionsSetup) {
            PermissionsSetupScreen(
                onFinished = {
                    navController.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.PermissionsSetup) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRoute.Home) {
            HomePlaceholderScreen(
                onOpenHealth = { navController.navigate(AppRoute.ReminderHealth) },
                onOpenSafAgain = { navController.navigate(AppRoute.SafSetup) }
            )
        }
        composable(AppRoute.ReminderHealth) {
            ReminderHealthScreen(onBack = { navController.popBackStack() })
        }
    }
}
