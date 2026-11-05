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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.data.backup.CleartextLegacyGrace
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.CarnetTipCard
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PinnedBackScaffold
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors
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
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = A11yTokens.PaperCard,
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
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = A11yTokens.PaperCard,
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
                                    "application/octet-stream",
                                    "*/*"
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
    val sectionShape = MaterialTheme.shapes.large
    PinnedBackScaffold(onBack = onBack, backEnabled = !state.busy) {
        CarnetPageTitle("Restaurer une sauvegarde")
        CarnetTipCard(
            text = "À utiliser après une réinstallation ou sur un nouveau téléphone. " +
                "Cette action remplace tout le carnet. " +
                "Si la copie vient d’un mail ou de WhatsApp, entrez le code noté à part."
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .shadow(2.dp, sectionShape, clip = false)
                .clip(sectionShape)
                .background(A11yTokens.PaperCard)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Code de partage",
                style = MaterialTheme.typography.titleMedium,
                color = A11yTokens.Ink
            )
            OutlinedTextField(
                value = state.shareCodeDraft,
                onValueChange = viewModel::onShareCodeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight),
                enabled = !state.busy,
                singleLine = true,
                label = { Text("Code noté sur papier") },
                placeholder = { Text("8 chiffres du téléphone d’origine") },
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = MaterialTheme.shapes.medium,
                colors = carnetOutlinedTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
            )
            if (CleartextLegacyGrace.OPT_IN_AVAILABLE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = state.allowCleartextLegacy,
                        onCheckedChange = viewModel::onAllowCleartextLegacyChange,
                        enabled = !state.busy,
                        colors = CheckboxDefaults.colors(
                            checkedColor = A11yTokens.ButtonFill,
                            uncheckedColor = A11yTokens.FieldLine,
                            checkmarkColor = A11yTokens.ButtonText
                        )
                    )
                    Text(
                        text = RestoreMessages.CLEARTEXT_GRACE_CHECKBOX,
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.InkMuted,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
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
                            if (err) A11yTokens.PaperCard else A11yTokens.AccentOkSoft,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
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
        // RST01 : CTA destructif hors Primary teal — Danger / Secondary fort.
        DangerA11yButton(
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
