package be.monmoisclair.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.data.reminder.ReminderHealthReport
import be.monmoisclair.app.ui.components.PinnedActionScaffold
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

    PinnedActionScaffold(
        primaryLabel = if (state.completing) "Finalisation…" else "Continuer",
        primaryEnabled = !state.completing && report != null,
        onPrimary = {
            val healthy = report?.isFullyHealthy == true
            viewModel.finishOnboarding(allowContinueWithWarnings = !healthy)
        }
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mon Mois Clair",
            style = MaterialTheme.typography.labelLarge,
            color = A11yTokens.InkMuted
        )
        Text(text = "Alertes fiables", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Pour que les rappels sonnent à l’heure : autorisez les notifications, " +
                "le réveil à l’heure, et laissez la batterie non limitée pour cette application.",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted
        )

        if (report != null) {
            HealthChecklist(report)
        } else {
            Text(
                text = "Vérification…",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted
            )
        }

        state.errorMessage?.let { msg ->
            Text(
                text = msg,
                color = A11yTokens.AccentDanger,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Assertive
                    contentDescription = msg
                }
            )
        }

        // Corrections = secondaires ; une seule primaire = Continuer (épinglé).
        if (report != null && !report.notificationsGranted && Build.VERSION.SDK_INT >= 33) {
            SecondaryA11yButton(
                text = "Autoriser les notifications",
                onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            )
        } else if (report != null && !report.notificationsGranted) {
            SecondaryA11yButton(
                text = "Ouvrir les réglages notifications",
                onClick = { context.startActivity(viewModel.intentNotifications()) }
            )
        }

        if (report != null && !report.exactAlarmGranted) {
            SecondaryA11yButton(
                text = "Autoriser le réveil à l’heure",
                onClick = { context.startActivity(viewModel.intentExactAlarm()) }
            )
        }

        if (report != null && !report.batteryOptimizationIgnored) {
            SecondaryA11yButton(
                text = "Laisser la batterie non limitée",
                onClick = { context.startActivity(viewModel.intentIgnoreBattery()) }
            )
        }

        if (report?.samsungDeviceCareHintNeeded == true) {
            Text(
                text = "Dans Samsung, retirez Mon Mois Clair de la mise en veille " +
                    "(Paramètres → Batterie → Limites d’utilisation en arrière-plan).",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SecondaryA11yButton(
            text = "Actualiser l’état",
            onClick = { viewModel.refresh() }
        )

        if (report != null && !report.isFullyHealthy) {
            Text(
                text = "Vous pouvez continuer, mais les rappels risquent de ne pas sonner " +
                    "tant que tous les points ne sont pas prêts.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HealthChecklist(report: ReminderHealthReport) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HealthStatusRow("Notifications", report.notificationsGranted)
        HealthStatusRow("Réveil à l’heure", report.exactAlarmGranted)
        HealthStatusRow("Batterie non limitée", report.batteryOptimizationIgnored)
    }
}
