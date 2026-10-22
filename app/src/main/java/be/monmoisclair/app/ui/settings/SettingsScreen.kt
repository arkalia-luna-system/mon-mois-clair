package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

/**
 * Hub Réglages : menu court → sous-écrans (un sujet chacun).
 * Alertes / Aide / Restaurer restent ici (pas dans le Menu Accueil).
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenHealth: () -> Unit,
    onOpenDeadlines: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenHelp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val fit = rememberDisplayFit()
    val tipPad = if (fit.compactHomeChrome) 10.dp else 12.dp

    SettingsResumeRefresh(viewModel)

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 2.dp else 4.dp))
        Text(
            text = SettingsHubCopy.TITLE,
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )

        SettingsHubRow(label = SettingsHubCopy.APPEARANCE, onClick = onOpenAppearance)
        SettingsHubRow(label = SettingsHubCopy.SAVINGS, onClick = onOpenSavings)
        SettingsHubRow(label = SettingsHubCopy.PRIVACY, onClick = onOpenPrivacy)
        SettingsHubRow(label = SettingsHubCopy.ALERTS, onClick = onOpenHealth)
        SettingsHubRow(label = SettingsHubCopy.DEADLINES, onClick = onOpenDeadlines)
        if (state.showSummaryButton) {
            SettingsHubRow(label = SettingsHubCopy.SUMMARY, onClick = onOpenSummary)
        }
        SettingsHubRow(label = SettingsHubCopy.BACKUP, onClick = onOpenBackup)
        SettingsHubRow(label = SettingsHubCopy.HELP, onClick = onOpenHelp)

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
                width = if (fit.compactHomeChrome) 140.dp else 160.dp,
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

/** Ligne hub : titre + › , filet, cible ≥ 48 dp. Pas de carte clone. */
@Composable
private fun SettingsHubRow(
    label: String,
    onClick: () -> Unit
) {
    HorizontalDivider(color = A11yTokens.PaperBorder)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(vertical = 4.dp)
            .semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = A11yTokens.Ink,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Link
        )
    }
}
