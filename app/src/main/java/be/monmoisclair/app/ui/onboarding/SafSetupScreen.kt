package be.monmoisclair.app.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
            .padding(A11yTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Mon Mois Clair", style = MaterialTheme.typography.displayLarge)
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
        Spacer(modifier = Modifier.weight(1f))
        PrimaryA11yButton(
            text = if (state.saving) "Enregistrement…" else "Choisir un dossier",
            enabled = !state.saving,
            onClick = { launcher.launch(viewModel.openDocumentTreeIntent()) }
        )
        if (onCancel != null) {
            SecondaryA11yButton(
                text = "Retour",
                enabled = !state.saving,
                onClick = onCancel
            )
        }
    }
}
