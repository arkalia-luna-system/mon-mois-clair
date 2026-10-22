package be.monmoisclair.app.ui.health

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.settings.DeviceCareCopy
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
fun ReminderHealthScreen(
    onBack: () -> Unit,
    viewModel: ReminderHealthViewModel = hiltViewModel()
) {
    val report by viewModel.report.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fit = rememberDisplayFit()
    val tipPad = if (fit.compactHomeChrome) 12.dp else 14.dp

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PinnedBackScaffold(onBack = onBack) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 2.dp else 4.dp))
        AppWordmark(width = if (fit.compactHomeChrome) 200.dp else 240.dp)
        Text(
            text = "Alertes",
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )
        val introShape = MaterialTheme.shapes.medium
        Text(
            text = "Vérifiez que Mon Mois Clair peut vraiment vous prévenir sur ce téléphone.",
            style = MaterialTheme.typography.bodyLarge,
            color = A11yTokens.InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, introShape)
                .background(A11yTokens.PaperCard, introShape)
                .padding(tipPad)
        )
        val scheduleShape = MaterialTheme.shapes.medium
        Text(
            text = "3 jours avant à 10 h, le jour même à 9 h, puis chaque matin à 10 h " +
                "tant que ce n’est pas payé.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, scheduleShape)
                .background(A11yTokens.PaperCard, scheduleShape)
                .padding(if (fit.compactHomeChrome) 10.dp else 12.dp)
        )

        val r = report
        if (r == null) {
            val busyShape = MaterialTheme.shapes.medium
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, busyShape)
                    .background(A11yTokens.PaperCard, busyShape)
                    .padding(12.dp)
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
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HealthStatusRow("Notifications", r.notificationsGranted)
                HealthStatusRow("Réveil à l’heure", r.exactAlarmGranted)
                HealthStatusRow("Batterie non limitée", r.batteryOptimizationIgnored)
            }
            if (!r.safBackupOk) {
                val safShape = MaterialTheme.shapes.medium
                Text(
                    text = "La sauvegarde se vérifie dans Réglages.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.PaperBorder, safShape)
                        .background(A11yTokens.PaperCard, safShape)
                        .padding(12.dp)
                )
            }
            if (r.samsungDeviceCareHintNeeded) {
                Text(
                    text = DeviceCareCopy.HINT,
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.AccentWarn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.AccentWarn.copy(alpha = 0.45f), MaterialTheme.shapes.medium)
                        .background(A11yTokens.PaperWarm, MaterialTheme.shapes.medium)
                        .padding(12.dp)
                )
            }
            if (r.isFullyHealthy) {
                val okShape = MaterialTheme.shapes.medium
                Text(
                    text = "Tout est prêt pour les alertes.",
                    color = A11yTokens.AccentOkInk,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, A11yTokens.AccentOkInk.copy(alpha = 0.4f), okShape)
                        .background(A11yTokens.AccentOkSoft, okShape)
                        .padding(12.dp)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = "Tout est prêt pour les alertes."
                        }
                )
            }
        }

        SecondaryA11yButton(text = "Actualiser", onClick = { viewModel.refresh() })
        // Une seule primaire dans l’app = actions de correction en secondaire (contour).
        if (report?.notificationsGranted == false) {
            SecondaryA11yButton(
                text = "Réglages notifications",
                onClick = { context.startActivity(viewModel.intentNotifications()) }
            )
        }
        if (report?.exactAlarmGranted == false) {
            SecondaryA11yButton(
                text = "Réglages réveil à l’heure",
                onClick = { context.startActivity(viewModel.intentExactAlarm()) }
            )
        }
        if (report?.batteryOptimizationIgnored == false) {
            SecondaryA11yButton(
                text = "Batterie non limitée",
                onClick = { context.startActivity(viewModel.intentIgnoreBattery()) }
            )
        }
    }
}
