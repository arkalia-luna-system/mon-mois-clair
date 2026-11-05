package be.monmoisclair.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.CarnetTipCard
import be.monmoisclair.app.ui.components.PinnedActionScaffold
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.settings.DeviceCareCopy
import be.monmoisclair.app.ui.health.HealthStatusRow
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
fun PermissionsSetupScreen(
    onFinished: () -> Unit,
    viewModel: PermissionsSetupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val report = state.report
    val fit = rememberDisplayFit()
    val tipPad = fit.tipPadLg

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
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 4.dp else 8.dp))
        AppWordmark(width = if (fit.compactHomeChrome) 200.dp else 240.dp)
        CarnetPageTitle("Alertes fiables", withTopSpacer = false)
        CarnetTipCard(
            text = "Pour que les rappels sonnent à l’heure : autorisez les notifications, " +
                "le réveil à l’heure, et laissez la batterie non limitée pour cette application."
        )

        if (report != null) {
            HealthChecklist(report)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .semantics {
                        contentDescription = "Vérification des alertes"
                        liveRegion = LiveRegionMode.Polite
                    }
            ) {
                CircularProgressIndicator(
                    color = A11yTokens.Link,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Vérification…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted
                )
            }
        }

        state.errorMessage?.let { msg ->
            CarnetStateBanner(text = msg, kind = CarnetBannerKind.Error, pad = 12.dp)
        }

        // ONB02 : un seul Secondary « corriger » à la fois (priorité notif → réveil → batterie).
        when {
            report != null && !report.notificationsGranted && Build.VERSION.SDK_INT >= 33 ->
                SecondaryA11yButton(
                    text = "Autoriser les notifications",
                    onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                )
            report != null && !report.notificationsGranted ->
                SecondaryA11yButton(
                    text = "Ouvrir les réglages notifications",
                    onClick = {
                        runCatching { context.startActivity(viewModel.intentNotifications()) }
                            .onFailure { viewModel.reportSettingsIntentFailed() }
                    }
                )
            report != null && !report.exactAlarmGranted ->
                SecondaryA11yButton(
                    text = "Autoriser le réveil à l’heure",
                    onClick = {
                        runCatching { context.startActivity(viewModel.intentExactAlarm()) }
                            .onFailure { viewModel.reportSettingsIntentFailed() }
                    }
                )
            report != null && !report.batteryOptimizationIgnored ->
                SecondaryA11yButton(
                    text = "Laisser la batterie non limitée",
                    onClick = {
                        runCatching { context.startActivity(viewModel.intentIgnoreBattery()) }
                            .onFailure { viewModel.reportSettingsIntentFailed() }
                    }
                )
            else ->
                SecondaryA11yButton(
                    text = "Actualiser l’état",
                    onClick = { viewModel.refresh() }
                )
        }

        if (report?.samsungDeviceCareHintNeeded == true) {
            Text(
                text = DeviceCareCopy.HINT,
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.AccentWarn,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.AccentWarn.copy(alpha = 0.45f), MaterialTheme.shapes.medium)
                    .background(A11yTokens.PaperWarm, MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }

        if (report != null && !report.isFullyHealthy) {
            Text(
                text = "Vous pouvez continuer, mais les rappels risquent de ne pas sonner " +
                    "tant que tous les points ne sont pas prêts.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.AccentWarn,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.AccentWarn.copy(alpha = 0.45f), MaterialTheme.shapes.medium)
                    .background(A11yTokens.PaperWarm, MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription =
                            "Vous pouvez continuer, mais les rappels risquent de ne pas sonner."
                    }
            )
        }
    }
}

@Composable
private fun HealthChecklist(report: ReminderHealthReport) {
    Column(verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)) {
        HealthStatusRow("Notifications", report.notificationsGranted)
        HealthStatusRow("Réveil à l’heure", report.exactAlarmGranted)
        HealthStatusRow("Batterie non limitée", report.batteryOptimizationIgnored)
    }
}
