package be.monmoisclair.app.ui.restore

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens

private enum class PendingRestore {
    Latest,
    File
}

@Composable
fun RestoreBackupScreen(
    onBack: () -> Unit,
    onRestored: () -> Unit,
    viewModel: RestoreBackupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pending by remember { mutableStateOf<PendingRestore?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val openDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        viewModel.restoreFromUri(uri)
    }

    // Afficher le dialogue de succès (ne pas quitter silencieusement).
    LaunchedEffect(state.success) {
        if (state.success) showSuccessDialog = true
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* forcer un OK explicite */ },
            containerColor = A11yTokens.PaperWarm,
            title = { Text("Restauration réussie") },
            text = {
                Text(
                    state.message
                        ?: "Les données affichées viennent de la sauvegarde."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onRestored()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (pending != null) {
        AlertDialog(
            onDismissRequest = { if (!state.busy) pending = null },
            containerColor = A11yTokens.PaperWarm,
            title = { Text("Remplacer toutes les données ?") },
            text = {
                Text(
                    "La restauration remplace toutes les données de l’application " +
                        "(tous les mois) par le contenu de la sauvegarde. " +
                        "Le dossier de sauvegarde choisi sur ce téléphone est conservé. " +
                        "Cette action ne peut pas être annulée."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val action = pending
                        pending = null
                        when (action) {
                            PendingRestore.Latest -> viewModel.restoreFromLatest()
                            PendingRestore.File -> openDocument.launch(
                                arrayOf("application/json", "text/*", "*/*")
                            )
                            null -> Unit
                        }
                    },
                    enabled = !state.busy
                ) {
                    Text("Restaurer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pending = null },
                    enabled = !state.busy
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Restaurer une sauvegarde", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Pour retrouver vos mois après une réinstallation ou un nouveau téléphone.",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Restaurer remplace tout ce qui est dans l’app. " +
                "Un écran vous demandera de confirmer.",
            style = MaterialTheme.typography.bodyMedium,
            color = A11yTokens.InkMuted
        )

        state.message?.let { msg ->
            if (!showSuccessDialog) {
                Text(
                    text = msg,
                    color = if (state.isError) A11yTokens.AccentDanger else A11yTokens.AccentOk,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        PrimaryA11yButton(
            text = if (state.busy) "Restauration…" else "Restaurer la dernière sauvegarde",
            enabled = !state.busy,
            onClick = { pending = PendingRestore.Latest }
        )
        SecondaryA11yButton(
            text = "Choisir un fichier JSON…",
            enabled = !state.busy,
            onClick = { pending = PendingRestore.File }
        )
    }
}
