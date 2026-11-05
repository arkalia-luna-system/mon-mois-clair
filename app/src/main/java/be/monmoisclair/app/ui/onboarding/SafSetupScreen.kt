package be.monmoisclair.app.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.AppWordmark
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.PinnedActionScaffold
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

@Composable
fun SafSetupScreen(
    onSafReady: () -> Unit,
    onCancel: (() -> Unit)? = null,
    viewModel: SafSetupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fit = rememberDisplayFit()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onFolderPicked(result.data?.data)
    }

    LaunchedEffect(state.completed) {
        if (state.completed) onSafReady()
    }

    PinnedActionScaffold(
        primaryLabel = if (state.saving) "Enregistrement…" else "Choisir un dossier",
        primaryEnabled = !state.saving,
        onPrimary = { launcher.launch(viewModel.openDocumentTreeIntent()) },
        secondaryLabel = if (onCancel != null) "Retour" else null,
        onSecondary = onCancel
    ) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 4.dp else 8.dp))
        AppWordmark(width = if (fit.compactHomeChrome) 200.dp else 240.dp)
        CarnetPageTitle("Dossier de sauvegarde", withTopSpacer = false)
        // ONB01 : 2 phrases — tip mist (rythme Aide / Backup).
        Text(
            text = "Choisissez un dossier dans Documents (pas Téléchargements). " +
                "Trois copies y seront écrites à chaque changement.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(A11yTokens.PaperWarm)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        )
        if (state.saving) {
            Text(
                text = "Enregistrement du dossier…",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "Enregistrement du dossier"
                    }
            )
        }
        state.errorMessage?.let { msg ->
            val errShape = MaterialTheme.shapes.medium
            Text(
                text = msg,
                color = A11yTokens.DangerInk,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.DangerInk.copy(alpha = 0.4f), errShape)
                    .background(A11yTokens.PaperWarm, errShape)
                    .padding(12.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Assertive
                        contentDescription = msg
                    }
            )
        }
    }
}
