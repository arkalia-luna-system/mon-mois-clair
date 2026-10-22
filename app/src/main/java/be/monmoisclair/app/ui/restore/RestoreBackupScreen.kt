package be.monmoisclair.app.ui.restore

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

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
            shape = MaterialTheme.shapes.medium,
            containerColor = A11yTokens.PaperWarm,
            title = {
                Text(
                    text = "Restauration réussie",
                    color = A11yTokens.Ink
                )
            },
            text = {
                Text(
                    text = state.message
                        ?: "Les données affichées viennent de la sauvegarde.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Ink
                )
            },
            confirmButton = {
                PrimaryA11yButton(
                    text = "Voir le mois",
                    onClick = {
                        showSuccessDialog = false
                        onRestored()
                    },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            }
        )
    }

    if (pending != null) {
        AlertDialog(
            onDismissRequest = { if (!state.busy) pending = null },
            shape = MaterialTheme.shapes.medium,
            containerColor = A11yTokens.PaperWarm,
            title = {
                Text(
                    text = "Remplacer toutes les données ?",
                    color = A11yTokens.Ink
                )
            },
            text = {
                Text(
                    text = "La restauration remplace toutes les données de l’application " +
                        "(tous les mois) par le contenu de la sauvegarde. " +
                        "Le dossier de sauvegarde choisi sur ce téléphone est conservé. " +
                        "Cette action ne peut pas être annulée.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Ink
                )
            },
            confirmButton = {
                DangerA11yButton(
                    text = "Restaurer",
                    onClick = {
                        val action = pending
                        pending = null
                        when (action) {
                            PendingRestore.Latest -> viewModel.restoreFromLatest()
                            PendingRestore.File -> openDocument.launch(
                                arrayOf(
                                    "application/zip",
                                    "application/json",
                                    "application/octet-stream"
                                )
                            )
                            null -> Unit
                        }
                    },
                    enabled = !state.busy,
                    fillMaxWidth = false,
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            },
            dismissButton = {
                SecondaryA11yButton(
                    text = "Annuler",
                    onClick = { pending = null },
                    enabled = !state.busy,
                    fillMaxWidth = false,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        )
    }

    val fit = rememberDisplayFit()
    val tipPad = if (fit.compactHomeChrome) 12.dp else 14.dp
    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        Spacer(modifier = Modifier.height(if (fit.compactHomeChrome) 2.dp else 4.dp))
        Text(
            text = "Restaurer une sauvegarde",
            style = MaterialTheme.typography.headlineMedium,
            color = A11yTokens.Ink
        )
        val tipShape = MaterialTheme.shapes.medium
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, A11yTokens.PaperBorder, tipShape)
                .background(A11yTokens.PaperCard, tipShape)
                .padding(tipPad),
            verticalArrangement = Arrangement.spacedBy(if (fit.compactHomeChrome) 6.dp else 8.dp)
        ) {
            Text(
                text = "Pour retrouver vos mois après une réinstallation ou un nouveau téléphone.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted
            )
            Text(
                text = "Restaurer remplace tout ce qui est dans l’app. " +
                    "Un écran vous demandera de confirmer.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
        }

        state.message?.let { msg ->
            if (!showSuccessDialog) {
                val msgShape = MaterialTheme.shapes.medium
                val err = state.isError
                Text(
                    text = msg,
                    color = if (err) A11yTokens.DangerInk else A11yTokens.AccentOkInk,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (err) A11yTokens.DangerInk.copy(alpha = 0.4f)
                            else A11yTokens.AccentOkInk.copy(alpha = 0.35f),
                            msgShape
                        )
                        .background(
                            if (err) A11yTokens.PaperWarm else A11yTokens.AccentOkSoft,
                            msgShape
                        )
                        .padding(12.dp)
                        .semantics {
                            liveRegion = if (err) {
                                LiveRegionMode.Assertive
                            } else {
                                LiveRegionMode.Polite
                            }
                            contentDescription = msg
                        }
                )
            }
        }

        if (state.busy) {
            Spacer(modifier = Modifier.height(8.dp))
            val busyShape = MaterialTheme.shapes.medium
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, busyShape)
                    .background(A11yTokens.PaperCard, busyShape)
                    .padding(12.dp)
                    .semantics {
                        contentDescription = "Restauration en cours"
                        liveRegion = LiveRegionMode.Polite
                    }
            ) {
                CircularProgressIndicator(
                    color = A11yTokens.Link,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Restauration…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted
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
            text = "Choisir une copie de sauvegarde…",
            enabled = !state.busy,
            onClick = { pending = PendingRestore.File }
        )
    }
}
