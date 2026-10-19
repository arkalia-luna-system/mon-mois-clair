package be.monmoisclair.app.ui.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.data.reminder.ReminderHealthReport
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.health.HealthStatusRow
import be.monmoisclair.app.ui.theme.A11yTokens

@Composable
fun PermissionsSetupScreen(
    onFinished: () -> Unit,
    viewModel: PermissionsSetupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val report = state.report

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.refresh() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.completed) {
        if (state.completed) onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
            .verticalScroll(rememberScrollState())
            .padding(A11yTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Mon Mois Clair", style = MaterialTheme.typography.displayLarge)
        Text(text = "Rappels fiables", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Sur Samsung, il faut autoriser les notifications, les alarmes exactes " +
                "et désactiver l’optimisation de batterie pour cette application.",
            style = MaterialTheme.typography.bodyLarge
        )

        if (report != null) {
            HealthChecklist(report)
        }

        state.errorMessage?.let { msg ->
            Text(text = msg, color = A11yTokens.AccentDanger, style = MaterialTheme.typography.bodyLarge)
        }

        if (report != null && !report.notificationsGranted && Build.VERSION.SDK_INT >= 33) {
            PrimaryA11yButton(
                text = "Autoriser les notifications",
                onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            )
        } else if (report != null && !report.notificationsGranted) {
            PrimaryA11yButton(
                text = "Ouvrir les réglages notifications",
                onClick = { context.startActivity(viewModel.intentNotifications()) }
            )
        }

        if (report != null && !report.exactAlarmGranted) {
            PrimaryA11yButton(
                text = "Autoriser les alarmes exactes",
                onClick = { context.startActivity(viewModel.intentExactAlarm()) }
            )
        }

        if (report != null && !report.batteryOptimizationIgnored) {
            PrimaryA11yButton(
                text = "Désactiver l’optimisation batterie",
                onClick = { context.startActivity(viewModel.intentIgnoreBattery()) }
            )
        }

        if (report?.samsungDeviceCareHintNeeded == true) {
            Text(
                text = "Samsung Device Care : ouvrez aussi Paramètres → Batterie → " +
                    "Limites d’utilisation en arrière-plan, et retirez Mon Mois Clair " +
                    "de « Mise en veille » / « Mise en veille profonde ».",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SecondaryA11yButton(
            text = "Actualiser l’état",
            onClick = { viewModel.refresh() }
        )

        PrimaryA11yButton(
            text = if (state.completing) "Finalisation…" else "Continuer",
            enabled = !state.completing && report != null,
            onClick = {
                val healthy = report?.isFullyHealthy == true
                viewModel.finishOnboarding(allowContinueWithWarnings = !healthy)
            }
        )

        if (report != null && !report.isFullyHealthy) {
            Text(
                text = "Vous pouvez continuer, mais les rappels risquent de ne pas sonner " +
                    "tant que tous les points ne sont pas OK.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HealthChecklist(report: ReminderHealthReport) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HealthStatusRow("Notifications", report.notificationsGranted)
        HealthStatusRow("Alarmes exactes", report.exactAlarmGranted)
        HealthStatusRow("Batterie non limitée", report.batteryOptimizationIgnored)
        HealthStatusRow("Sauvegarde Documents", report.safBackupOk)
    }
}
