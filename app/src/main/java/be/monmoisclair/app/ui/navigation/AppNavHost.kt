package be.monmoisclair.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import be.monmoisclair.app.domain.usecase.StartDestination
import be.monmoisclair.app.ui.entry.AddEntryScreen
import be.monmoisclair.app.ui.entry.EditEntryScreen
import be.monmoisclair.app.ui.health.ReminderHealthScreen
import be.monmoisclair.app.ui.home.HomeScreen
import be.monmoisclair.app.ui.onboarding.PermissionsSetupScreen
import be.monmoisclair.app.ui.onboarding.SafSetupScreen
import be.monmoisclair.app.ui.restore.RestoreBackupScreen
import be.monmoisclair.app.ui.settings.SettingsScreen

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
            val fromHome = navController.previousBackStackEntry != null
            SafSetupScreen(
                onSafReady = {
                    if (fromHome) {
                        // Re-choix dossier depuis Home : ne pas rejouer l’onboarding permissions.
                        navController.popBackStack()
                    } else {
                        navController.navigate(AppRoute.PermissionsSetup) {
                            popUpTo(AppRoute.SafSetup) { inclusive = true }
                        }
                    }
                },
                onCancel = if (fromHome) {
                    { navController.popBackStack() }
                } else {
                    null
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
            HomeScreen(
                onAdd = { navController.navigate(AppRoute.AddEntry) },
                onEdit = { id, isExpense ->
                    navController.navigate(AppRoute.editEntry(id, isExpense))
                },
                onOpenHealth = { navController.navigate(AppRoute.ReminderHealth) },
                onOpenSettings = { navController.navigate(AppRoute.Settings) }
            )
        }
        composable(AppRoute.Settings) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenHealth = { navController.navigate(AppRoute.ReminderHealth) },
                onOpenSaf = { navController.navigate(AppRoute.SafSetup) },
                onOpenRestore = { navController.navigate(AppRoute.RestoreBackup) }
            )
        }
        composable(AppRoute.ReminderHealth) {
            ReminderHealthScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoute.RestoreBackup) {
            RestoreBackupScreen(
                onBack = { navController.popBackStack() },
                onRestored = {
                    // Home reste sous la pile : pop Restore → Flows Room se mettent à jour.
                    navController.popBackStack(AppRoute.Home, inclusive = false)
                }
            )
        }
        composable(AppRoute.AddEntry) {
            AddEntryScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = AppRoute.EditEntry,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("isExpense") { type = NavType.StringType }
            )
        ) {
            EditEntryScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
