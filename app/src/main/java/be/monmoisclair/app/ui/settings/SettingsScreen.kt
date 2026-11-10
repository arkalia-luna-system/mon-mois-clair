package be.monmoisclair.app.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

internal const val SETTINGS_LOCAL_ONLY_A11Y =
    "Mon Mois Clair. Données sur cet appareil uniquement."
internal const val SETTINGS_LOCAL_ONLY_BODY =
    "Données locales — pas de compte, pas de cloud."

/**
 * Hub Réglages : accès directs + groupes courts.
 * Aide s'ouvre aussi depuis le Menu Accueil, mais reste disponible ici.
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
            leadingIconRes = R.drawable.ic_backup,
            leadingColoredIcon = true
        )
        CarnetSectionTitle("Groupes")
        CarnetHubRow(
            label = SettingsHubCopy.GROUP_ACCOUNT,
            onClick = onOpenAccountGroup,
            leadingIconRes = R.drawable.ic_kit_profil,
            leadingColoredIcon = true
        )
        CarnetHubRow(
            label = SettingsHubCopy.GROUP_COMFORT,
            onClick = onOpenComfortGroup,
            leadingIconRes = R.drawable.ic_comfort,
            leadingColoredIcon = true
        )
        CarnetHubRow(
            label = SettingsHubCopy.GROUP_HELP,
            onClick = onOpenHelp,
            leadingIconRes = R.drawable.ic_help,
            leadingColoredIcon = true
        )

        val versionName = remember {
            runCatching {
                val pm = context.packageManager
                val pkg = context.packageName
                val info = if (Build.VERSION.SDK_INT >= 33) {
                    pm.getPackageInfo(
                        pkg,
                        android.content.pm.PackageManager.PackageInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(pkg, 0)
                }
                info.versionName
            }.getOrNull()
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
                        append(". ")
                        append(SETTINGS_LOCAL_ONLY_A11Y.removePrefix("Mon Mois Clair. "))
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
                text = SETTINGS_LOCAL_ONLY_BODY,
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
            showDivider = false,
            leadingIconRes = R.drawable.ic_backup,
            leadingColoredIcon = true
        )
        CarnetHubRow(
            label = SettingsHubCopy.DEADLINES,
            onClick = onOpenDeadlines,
            leadingIconRes = R.drawable.ic_upcoming,
            leadingColoredIcon = true
        )
        CarnetHubRow(
            label = SettingsHubCopy.SAVINGS_ROW,
            onClick = onOpenSavings,
            leadingIconRes = R.drawable.ic_piggy,
            leadingColoredIcon = true
        )
        CarnetHubRow(
            label = SettingsHubCopy.PRIVACY,
            onClick = onOpenPrivacy,
            leadingIconRes = R.drawable.ic_privacy,
            leadingColoredIcon = true
        )
        if (state.showSummaryButton) {
            CarnetHubRow(
                label = SettingsHubCopy.SUMMARY,
                onClick = onOpenSummary,
                leadingIconRes = R.drawable.ic_data,
                leadingColoredIcon = true
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
            showDivider = false,
            leadingIconRes = R.drawable.ic_comfort,
            leadingColoredIcon = true
        )
        CarnetHubRow(
            label = SettingsHubCopy.ALERTS,
            onClick = onOpenHealth,
            leadingIconRes = R.drawable.ic_alerts,
            leadingColoredIcon = true,
            labelColor = if (state.remindersBlocked) {
                A11yTokens.AccentWarn
            } else {
                A11yTokens.Ink
            }
        )
    }
}
