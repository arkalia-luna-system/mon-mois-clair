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
import be.monmoisclair.app.ui.dossiers.ConvertPendingLetterScreen
import be.monmoisclair.app.ui.dossiers.DossiersScreen
import be.monmoisclair.app.ui.dossiers.PendingLetterFormScreen
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
import androidx.hilt.navigation.compose.hiltViewModel
import be.monmoisclair.app.ui.settings.HowItWorksScreen
import be.monmoisclair.app.ui.settings.SettingsAppearanceScreen
import be.monmoisclair.app.ui.settings.SettingsBackupScreen
import be.monmoisclair.app.ui.settings.SettingsDeadlinesScreen
import be.monmoisclair.app.ui.settings.SettingsHelpScreen
import be.monmoisclair.app.ui.settings.SettingsPrivacyScreen
import be.monmoisclair.app.ui.settings.SettingsSavingsScreen
import be.monmoisclair.app.ui.settings.SettingsScreen
import be.monmoisclair.app.ui.settings.SettingsSummaryScreen
import be.monmoisclair.app.ui.upcoming.UpcomingScreen

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
                onOpenDossiers = { navController.navigate(AppRoute.Dossiers) },
                onOpenUpcoming = { navController.navigate(AppRoute.Upcoming) },
                onOpenSavings = { navController.navigate(AppRoute.SettingsSavings) },
                onOpenDeadlines = { navController.navigate(AppRoute.SettingsDeadlines) }
            )
        }
        composable(AppRoute.Upcoming) {
            UpcomingScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoute.Dossiers) {
            DossiersScreen(
                onBack = { navController.popBackStack() },
                onOpenInstance = { id ->
                    navController.navigate(AppRoute.editEntry(id, isExpense = true))
                },
                onOpenTemplate = { id ->
                    navController.navigate(AppRoute.editTemplate(id))
                },
                onConvertPending = { letterId ->
                    navController.navigate(AppRoute.convertPendingLetter(letterId))
                },
                onAddPending = {
                    navController.navigate(AppRoute.PendingLetterForm)
                }
            )
        }
        composable(AppRoute.PendingLetterForm) {
            PendingLetterFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = AppRoute.ConvertPendingLetter,
            arguments = listOf(navArgument("letterId") { type = NavType.StringType })
        ) {
            ConvertPendingLetterScreen(
                onBack = { navController.popBackStack() },
                onConverted = { instanceId ->
                    if (instanceId != null) {
                        navController.navigate(AppRoute.editEntry(instanceId, isExpense = true)) {
                            popUpTo(AppRoute.Dossiers) { inclusive = false }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
        composable(AppRoute.Settings) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenAppearance = { navController.navigate(AppRoute.SettingsAppearance) },
                onOpenSavings = { navController.navigate(AppRoute.SettingsSavings) },
                onOpenPrivacy = { navController.navigate(AppRoute.SettingsPrivacy) },
                onOpenHealth = { navController.navigate(AppRoute.ReminderHealth) },
                onOpenDeadlines = { navController.navigate(AppRoute.SettingsDeadlines) },
                onOpenSummary = { navController.navigate(AppRoute.SettingsSummary) },
                onOpenBackup = { navController.navigate(AppRoute.SettingsBackup) },
                // Aide → profondeur Comment ça marche (support reste accessible depuis Aide).
                onOpenHelp = { navController.navigate(AppRoute.HowItWorks) }
            )
        }
        composable(AppRoute.SettingsAppearance) { entry ->
            val parent = remember(entry) { navController.getBackStackEntry(AppRoute.Settings) }
            SettingsAppearanceScreen(
                onBack = { navController.popBackStack() },
                viewModel = hiltViewModel(parent)
            )
        }
        composable(AppRoute.SettingsSavings) { entry ->
            // Depuis Accueil sticky ou hub Réglages (parent si présent).
            val owner = remember(entry) {
                runCatching { navController.getBackStackEntry(AppRoute.Settings) }
                    .getOrDefault(entry)
            }
            SettingsSavingsScreen(
                onBack = { navController.popBackStack() },
                viewModel = hiltViewModel(owner)
            )
        }
        composable(AppRoute.SettingsPrivacy) { entry ->
            val parent = remember(entry) { navController.getBackStackEntry(AppRoute.Settings) }
            SettingsPrivacyScreen(
                onBack = { navController.popBackStack() },
                viewModel = hiltViewModel(parent)
            )
        }
        composable(AppRoute.SettingsDeadlines) { entry ->
            val owner = remember(entry) {
                runCatching { navController.getBackStackEntry(AppRoute.Settings) }
                    .getOrDefault(entry)
            }
            SettingsDeadlinesScreen(
                onBack = { navController.popBackStack() },
                onEditTemplate = { id -> navController.navigate(AppRoute.editTemplate(id)) },
                viewModel = hiltViewModel(owner)
            )
        }
        composable(AppRoute.SettingsSummary) { entry ->
            val parent = remember(entry) { navController.getBackStackEntry(AppRoute.Settings) }
            SettingsSummaryScreen(
                onBack = { navController.popBackStack() },
                onOpenDossiers = { navController.navigate(AppRoute.Dossiers) },
                viewModel = hiltViewModel(parent)
            )
        }
        composable(AppRoute.SettingsBackup) { entry ->
            val parent = remember(entry) { navController.getBackStackEntry(AppRoute.Settings) }
            SettingsBackupScreen(
                onBack = { navController.popBackStack() },
                onOpenSaf = { navController.navigate(AppRoute.SafSetup) },
                onOpenRestore = { navController.navigate(AppRoute.RestoreBackup) },
                viewModel = hiltViewModel(parent)
            )
        }
        composable(AppRoute.SettingsHelp) { entry ->
            val parent = remember(entry) { navController.getBackStackEntry(AppRoute.Settings) }
            SettingsHelpScreen(
                onBack = { navController.popBackStack() },
                onOpenHowItWorks = { navController.navigate(AppRoute.HowItWorks) },
                viewModel = hiltViewModel(parent)
            )
        }
        composable(AppRoute.HowItWorks) {
            HowItWorksScreen(
                onBack = { navController.popBackStack() },
                onOpenSupport = { navController.navigate(AppRoute.SettingsHelp) }
            )
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
