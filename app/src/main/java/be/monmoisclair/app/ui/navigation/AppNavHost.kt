package be.monmoisclair.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import be.monmoisclair.app.domain.usecase.StartDestination
import be.monmoisclair.app.ui.dossiers.DossiersScreen
import be.monmoisclair.app.ui.entry.AddEntryScreen
import be.monmoisclair.app.ui.entry.EditEntryScreen
import be.monmoisclair.app.ui.entry.EditTemplateScreen
import be.monmoisclair.app.ui.health.ReminderHealthScreen
import be.monmoisclair.app.ui.home.HomeScreen
import be.monmoisclair.app.ui.onboarding.PermissionsSetupScreen
import be.monmoisclair.app.ui.onboarding.SafSetupScreen
import be.monmoisclair.app.ui.restore.RestoreBackupScreen
import be.monmoisclair.app.ui.scan.EpcScanHandoff
import be.monmoisclair.app.ui.scan.ScanEpcScreen
import be.monmoisclair.app.ui.settings.HowItWorksScreen
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
                    if (fromHome) navController.popBackStack()
                    else navController.navigate(AppRoute.PermissionsSetup) {
                        popUpTo(AppRoute.SafSetup) { inclusive = true }
                    }
                },
                onCancel = if (fromHome) ({ navController.popBackStack() }) else null
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
                onAdd = { ym -> navController.navigate(AppRoute.addEntry(ym)) },
                onEdit = { id, isExpense ->
                    navController.navigate(AppRoute.editEntry(id, isExpense))
                },
                onOpenSettings = { navController.navigate(AppRoute.Settings) },
                onOpenHealth = { navController.navigate(AppRoute.ReminderHealth) },
                onOpenDossiers = { navController.navigate(AppRoute.Dossiers) }
            )
        }
        composable(AppRoute.Dossiers) {
            DossiersScreen(
                onBack = { navController.popBackStack() },
                onOpenInstance = { id ->
                    navController.navigate(AppRoute.editEntry(id, isExpense = true))
                },
                onOpenTemplate = { id ->
                    navController.navigate(AppRoute.editTemplate(id))
                }
            )
        }
        composable(AppRoute.Settings) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenHealth = { navController.navigate(AppRoute.ReminderHealth) },
                onOpenSaf = { navController.navigate(AppRoute.SafSetup) },
                onOpenRestore = { navController.navigate(AppRoute.RestoreBackup) },
                onOpenHowItWorks = { navController.navigate(AppRoute.HowItWorks) },
                onEditTemplate = { id -> navController.navigate(AppRoute.editTemplate(id)) }
            )
        }
        composable(AppRoute.HowItWorks) {
            HowItWorksScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoute.ReminderHealth) {
            ReminderHealthScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoute.RestoreBackup) {
            RestoreBackupScreen(
                onBack = { navController.popBackStack() },
                onRestored = { navController.popBackStack(AppRoute.Home, inclusive = false) }
            )
        }
        composable(
            route = AppRoute.AddEntry,
            arguments = listOf(navArgument("ym") { type = NavType.StringType })
        ) { entry ->
            var epcRaw by remember { mutableStateOf<String?>(null) }
            DisposableEffect(entry) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        EpcScanHandoff.take()?.let { epcRaw = it }
                    }
                }
                entry.lifecycle.addObserver(observer)
                onDispose { entry.lifecycle.removeObserver(observer) }
            }
            AddEntryScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
                onScanEpc = { navController.navigate(AppRoute.ScanEpc) },
                epcRawResult = epcRaw,
                onEpcRawConsumed = { epcRaw = null }
            )
        }
        composable(
            route = AppRoute.EditTemplate,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            EditTemplateScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(AppRoute.ScanEpc) {
            ScanEpcScreen(
                onResult = { raw ->
                    EpcScanHandoff.offer(raw)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = AppRoute.EditEntry,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("isExpense") { type = NavType.StringType }
            )
        ) { entry ->
            var epcRaw by remember { mutableStateOf<String?>(null) }
            DisposableEffect(entry) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        EpcScanHandoff.take()?.let { epcRaw = it }
                    }
                }
                entry.lifecycle.addObserver(observer)
                onDispose { entry.lifecycle.removeObserver(observer) }
            }
            EditEntryScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
                onScanEpc = { navController.navigate(AppRoute.ScanEpc) },
                epcRawResult = epcRaw,
                onEpcRawConsumed = { epcRaw = null }
            )
        }
    }
}
