package be.monmoisclair.app.ui.health

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens

@Composable
fun ReminderHealthScreen(
    onBack: () -> Unit,
    viewModel: ReminderHealthViewModel = hiltViewModel()
) {
    val report by viewModel.report.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PinnedBackScaffold(onBack = onBack) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Santé des rappels", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Vérifiez que Mon Mois Clair peut vraiment vous alerter sur ce Samsung.",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Rappels : 3 jours avant (10 h), le jour J (9 h), puis chaque jour à 10 h " +
                "si une dépense reste « à payer » après l’échéance.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )

        report?.let { r ->
            HealthStatusRow("Notifications", r.notificationsGranted)
            HealthStatusRow("Alarmes exactes", r.exactAlarmGranted)
            HealthStatusRow("Batterie non limitée", r.batteryOptimizationIgnored)
            HealthStatusRow("Sauvegarde Documents", r.safBackupOk)
            if (r.samsungDeviceCareHintNeeded) {
                Text(
                    text = "Samsung Device Care : retirez l’app de « Mise en veille » " +
                        "et « Mise en veille profonde ».",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (r.isFullyHealthy) {
                Text(
                    text = "Tout est prêt pour les rappels.",
                    color = A11yTokens.AccentOk,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        SecondaryA11yButton(text = "Actualiser", onClick = { viewModel.refresh() })
        if (report?.notificationsGranted == false) {
            PrimaryA11yButton(
                text = "Réglages notifications",
                onClick = { context.startActivity(viewModel.intentNotifications()) }
            )
        }
        if (report?.exactAlarmGranted == false) {
            PrimaryA11yButton(
                text = "Réglages alarmes exactes",
                onClick = { context.startActivity(viewModel.intentExactAlarm()) }
            )
        }
        if (report?.batteryOptimizationIgnored == false) {
            PrimaryA11yButton(
                text = "Exemption batterie",
                onClick = { context.startActivity(viewModel.intentIgnoreBattery()) }
            )
        }
    }
}
