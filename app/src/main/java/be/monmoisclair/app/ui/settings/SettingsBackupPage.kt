package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.domain.model.BackupHealth
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

/**
 * Parcours Sauvegarde 60+ : 3 gestes clairs — noter le code, dossier, envoyer ;
 * restaurer à part (danger).
 */
@Composable
fun SettingsBackupScreen(
    onBack: () -> Unit,
    onOpenSaf: () -> Unit,
    onOpenRestore: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fit = rememberDisplayFit()
    val tipPad = fit.tipPad

    SettingsResumeRefresh(viewModel)
    SettingsShareEffect(viewModel)

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        SettingsPageTitle(SettingsHubCopy.BACKUP)
        if (state.dbMigrationFailed) {
            CarnetStateBanner(
                text = "Protection du carnet incomplète sur cet appareil. " +
                    "Vos données restent utilisables. Faites une copie maintenant, " +
                    "notez le code à part, puis réinstallez ou contactez Arkalia Système.",
                kind = CarnetBannerKind.Error,
                pad = tipPad
            )
        }
        if (state.busy) {
            Text(
                text = "Préparation de la copie…",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "Préparation de la copie"
                    }
            )
        }
        Text(
            text = "Trois étapes : 1) noter le code · 2) choisir le dossier · 3) envoyer une copie.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.backupHealth != BackupHealth.NOT_CONFIGURED) {
            Text(
                text = "Vos 3 dernières copies sont gardées dans Documents. " +
                    "Pour en ouvrir une ailleurs (WhatsApp ou mail), il faut le code ci-dessous.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
        }
        state.shareCopyCode?.let { code ->
            Text(
                text = "1. Code à noter une fois",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = code,
                style = MaterialTheme.typography.headlineMedium,
                color = A11yTokens.Ink,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Code de copie $code" }
            )
            Text(
                text = "Écrivez ce code sur papier une seule fois. " +
                    "Sans lui, la copie ne s’ouvre pas sur un autre téléphone. " +
                    "N’envoyez jamais le code avec le fichier. " +
                    "Si le téléphone est perdu et que le code n’a pas été noté, " +
                    "la copie ne pourra pas être relue.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted,
                modifier = Modifier.fillMaxWidth()
            )
        }
        run {
            val healthLine = BackupHealthCopy.statusLine(state.backupHealth)
            val healthOk = BackupHealthCopy.isOk(state.backupHealth)
            CarnetStateBanner(
                text = healthLine,
                kind = if (healthOk) CarnetBannerKind.Ok else CarnetBannerKind.Error,
                pad = tipPad
            )
        }
        SecondaryA11yButton(
            text = "2. Choisir le dossier",
            onClick = onOpenSaf,
            enabled = !state.busy
        )
        SecondaryA11yButton(
            text = "3. Envoyer une copie",
            onClick = viewModel::shareSecurityCopy,
            enabled = !state.busy
        )
        // SET04 : restaurer = Danger (destructif).
        DangerA11yButton(
            text = "Restaurer une sauvegarde",
            onClick = onOpenRestore,
            enabled = !state.busy
        )
        state.backupMessage?.let { msg ->
            CarnetStateBanner(
                text = msg,
                kind = if (state.backupMessageError) CarnetBannerKind.Error else CarnetBannerKind.Ok,
                pad = tipPad
            )
        }
        state.flashMessage?.let { msg ->
            CarnetStateBanner(
                text = msg,
                kind = if (state.flashMessageError) CarnetBannerKind.Error else CarnetBannerKind.Ok,
                pad = tipPad
            )
        }
    }
}
