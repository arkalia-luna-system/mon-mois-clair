package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.components.CarnetHubRow
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetSectionTitle
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

/**
 * Hub Réglages : 3 groupes → sous-écrans sujet.
 * Alertes / Aide / Restaurer restent ici (pas dans le Menu Accueil).
 * Échéances = Données (métier) ; Confort = Apparence + Alertes seulement.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAccountGroup: () -> Unit,
    onOpenComfortGroup: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenBackup: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val fit = rememberDisplayFit()
    val tipPad = fit.tipPad

    SettingsResumeRefresh(viewModel)

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        CarnetPageTitle(SettingsHubCopy.TITLE)
        // 7.12 : sections + leading icons (densité top-app).
        CarnetSectionTitle("Essentiel")
        CarnetHubRow(
            label = SettingsHubCopy.BACKUP_SHORTCUT,
            onClick = onOpenBackup,
            showDivider = false,
            leadingIconRes = R.drawable.ic_backup
        )
        CarnetSectionTitle("Groupes")
        CarnetHubRow(
            label = SettingsHubCopy.GROUP_ACCOUNT,
            onClick = onOpenAccountGroup,
            leadingIconRes = R.drawable.ic_data
        )
        CarnetHubRow(
            label = SettingsHubCopy.GROUP_COMFORT,
            onClick = onOpenComfortGroup,
            leadingIconRes = R.drawable.ic_comfort
        )
        CarnetHubRow(
            label = SettingsHubCopy.GROUP_HELP,
            onClick = onOpenHelp,
            leadingIconRes = R.drawable.ic_help
        )

        val versionName = try {
            val p = context.packageManager.getPackageInfo(context.packageName, 0)
            p.versionName
        } catch (_: Exception) {
            null
        }
        val stepLabel = AppVersionDisplay.stepLabel(versionName)
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .padding(vertical = tipPad)
                .semantics(mergeDescendants = true) {
                    contentDescription = buildString {
                        append("Mon Mois Clair")
                        if (stepLabel != null) append(", étape $stepLabel")
                        append(". Données sur ce téléphone uniquement.")
                    }
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppWordmark(
                // 7.14 : assez large pour « Clair » or (évite coupe à 140–160).
                width = if (fit.compactHomeChrome) 220.dp else 260.dp,
                announce = false
            )
            if (stepLabel != null) {
                Text(
                    text = stepLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = A11yTokens.InkMuted
                )
            }
            Text(
                text = "Données sur ce téléphone — pas de compte, pas de cloud.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
        }
    }
}

@Composable
fun SettingsAccountGroupScreen(
    onBack: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenDeadlines: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsResumeRefresh(viewModel)
    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.GROUP_ACCOUNT)
        CarnetHubRow(
            label = SettingsHubCopy.BACKUP,
            onClick = onOpenBackup,
            leadingIconRes = R.drawable.ic_backup
        )
        CarnetHubRow(
            label = SettingsHubCopy.DEADLINES,
            onClick = onOpenDeadlines,
            leadingIconRes = R.drawable.ic_upcoming
        )
        CarnetHubRow(
            label = SettingsHubCopy.SAVINGS_ROW,
            onClick = onOpenSavings,
            leadingIconRes = R.drawable.ic_piggy
        )
        CarnetHubRow(
            label = SettingsHubCopy.PRIVACY,
            onClick = onOpenPrivacy,
            leadingIconRes = R.drawable.ic_privacy
        )
        if (state.showSummaryButton) {
            CarnetHubRow(
                label = SettingsHubCopy.SUMMARY,
                onClick = onOpenSummary,
                leadingIconRes = R.drawable.ic_data
            )
        }
    }
}

@Composable
fun SettingsComfortGroupScreen(
    onBack: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenHealth: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsResumeRefresh(viewModel)
    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.GROUP_COMFORT)
        CarnetHubRow(
            label = SettingsHubCopy.APPEARANCE,
            onClick = onOpenAppearance,
            leadingIconRes = R.drawable.ic_comfort
        )
        CarnetHubRow(
            label = SettingsHubCopy.ALERTS,
            onClick = onOpenHealth,
            leadingIconRes = R.drawable.ic_alerts
        )
    }
}
