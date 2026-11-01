package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.domain.model.ThemePreference
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
fun SettingsAppearanceScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsResumeRefresh(viewModel)
    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.APPEARANCE)
        ThemeOptionRow(
            label = "Comme le téléphone",
            selected = state.themePreference == ThemePreference.System,
            enabled = !state.busy,
            onSelect = { viewModel.setThemePreference(ThemePreference.System) }
        )
        ThemeOptionRow(
            label = "Clair",
            selected = state.themePreference == ThemePreference.Light,
            enabled = !state.busy,
            onSelect = { viewModel.setThemePreference(ThemePreference.Light) }
        )
        ThemeOptionRow(
            label = "Sombre",
            selected = state.themePreference == ThemePreference.Dark,
            enabled = !state.busy,
            onSelect = { viewModel.setThemePreference(ThemePreference.Dark) }
        )
    }
}

@Composable
fun SettingsDeadlinesScreen(
    onBack: () -> Unit,
    onEditTemplate: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fit = rememberDisplayFit()
    val tipPadLg = fit.tipPadLg

    SettingsResumeRefresh(viewModel)

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.DEADLINES)
        if (state.offMonthPeriodics.isEmpty()) {
            Text(
                text = "Aucune pour l’instant.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)) {
                state.offMonthPeriodics.forEach { item ->
                    Surface(
                        color = A11yTokens.PaperWarm,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = A11yTokens.minTouchHeight)
                            .border(1.dp, A11yTokens.PaperBorder, MaterialTheme.shapes.medium)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Modifier ${item.label}"
                            ) { onEditTemplate(item.templateId) }
                    ) {
                        Column(
                            modifier = Modifier.padding(tipPadLg),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.titleLarge,
                                color = A11yTokens.Ink
                            )
                            Text(
                                text = viewModel.formatPeriodicAmountLine(item),
                                style = MaterialTheme.typography.bodyLarge,
                                color = A11yTokens.Ink
                            )
                            Text(
                                text = "Prochaine : ${item.nextDueLabel}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.AccentWarn
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSummaryScreen(
    onBack: () -> Unit,
    onOpenDossiers: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsResumeRefresh(viewModel)

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.SUMMARY)
        Text(
            text = "Texte ou PDF pour un créancier ou un conseiller — dans Dossiers. " +
                "PDF clair ≠ copie .mmc.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted,
            modifier = Modifier.fillMaxWidth()
        )
        QuietLinkA11y(
            text = "Ouvrir Dossiers",
            onClick = onOpenDossiers
        )
    }
}

@Composable
fun SettingsHelpScreen(
    onBack: () -> Unit,
    onOpenHowItWorks: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fit = rememberDisplayFit()
    val tipPad = fit.tipPad

    SettingsResumeRefresh(viewModel)
    SettingsShareEffect(viewModel)

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.HELP)
        SecondaryA11yButton(
            text = "Comment ça marche",
            onClick = onOpenHowItWorks,
            enabled = !state.busy
        )
        // Contact = Menu Accueil uniquement (pas de doublon Aide / HowItWorks).
        state.flashMessage?.let { msg ->
            CarnetStateBanner(
                text = msg,
                kind = if (state.flashMessageError) CarnetBannerKind.Error else CarnetBannerKind.Ok,
                pad = tipPad
            )
        }
    }
}
