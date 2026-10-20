package be.monmoisclair.app.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.PinnedActionScaffold
import be.monmoisclair.app.ui.theme.A11yTokens

@Composable
fun SafSetupScreen(
    onSafReady: () -> Unit,
    onCancel: (() -> Unit)? = null,
    viewModel: SafSetupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mon Mois Clair",
            style = MaterialTheme.typography.labelLarge,
            color = A11yTokens.InkMuted
        )
        Text(text = "Dossier de sauvegarde", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Choisissez un dossier dans Documents (pas Téléchargements / Download). " +
                "À chaque modification, une copie de sécurité y sera écrite automatiquement " +
                "(3 versions). Sans ce dossier, vos données peuvent être perdues.",
            style = MaterialTheme.typography.bodyLarge
        )
        state.errorMessage?.let { msg ->
            Text(
                text = msg,
                color = A11yTokens.AccentDanger,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
