package be.monmoisclair.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.domain.model.BackupHealth
import be.monmoisclair.app.ui.components.CarnetBannerKind
import be.monmoisclair.app.ui.components.CarnetSectionTitle
import be.monmoisclair.app.ui.components.CarnetStateBanner
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.HomeTokens
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
    val introShape = HomeTokens.cardRadius

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
        // 7.23 : intro en carte mist (rythme Accueil), pas mur plat.
        Text(
            text = "Trois étapes : 1) noter le code · 2) choisir le dossier · 3) envoyer une copie.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .clip(introShape)
                .background(A11yTokens.PaperWarm)
                .padding(horizontal = 14.dp, vertical = 12.dp)
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
                style = MaterialTheme.typography.titleMedium,
                color = A11yTokens.Ink,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = code,
                style = MaterialTheme.typography.headlineMedium,
                color = A11yTokens.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(HomeTokens.cardElevation, introShape, clip = false)
                    .clip(introShape)
                    .background(A11yTokens.PaperCard)
                    .border(1.5.dp, A11yTokens.AccentAuto, introShape)
                    .padding(horizontal = 16.dp, vertical = 18.dp)
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
        // 7.12 : section restaurer séparée (densité top-app) ; Danger = destructif.
        CarnetSectionTitle("Restaurer")
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
