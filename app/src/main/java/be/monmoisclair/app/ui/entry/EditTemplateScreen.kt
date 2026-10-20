package be.monmoisclair.app.ui.entry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.domain.RecurrenceAnchor
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.imeOpen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTemplateScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    viewModel: EditTemplateViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val keyboardOpen = imeOpen()
    var confirmArchive by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    if (confirmArchive) {
        val name = state.label.trim().ifBlank { "cette échéance" }
        AlertDialog(
            onDismissRequest = { confirmArchive = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = A11yTokens.PaperWarm,
            title = { Text("Retirer $name ?") },
            text = {
                Text(
                    "L’échéance ne reviendra plus les mois suivants. " +
                        "Les mois déjà notés sur l’accueil ne changent pas.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                DangerA11yButton(
                    text = "Retirer",
                    onClick = {
                        confirmArchive = false
                        viewModel.archive()
                    },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            },
            dismissButton = {
                SecondaryA11yButton(
                    text = "Annuler",
                    onClick = { confirmArchive = false },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        )
    }

    if (state.loading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(A11yTokens.screenPadding)
        ) {
            Text("Chargement…", style = MaterialTheme.typography.bodyLarge, color = A11yTokens.InkMuted)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(A11yTokens.screenPadding)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Modifier l’échéance", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = state.intervalHint,
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
            Text(
                text = "Les mois déjà notés sur l’accueil ne changent pas. " +
                    "Les mois suivants suivront ce modèle.",
                style = MaterialTheme.typography.bodyMedium,
                color = A11yTokens.InkMuted
            )
            OutlinedTextField(
                value = state.label,
                onValueChange = viewModel::setLabel,
                label = { Text("Nom") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = state.amountText,
                onValueChange = viewModel::setAmount,
                label = { Text("Montant (€)") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            DayOfMonthGrid(
                selectedDay = state.dayText.toIntOrNull(),
                onSelect = { viewModel.setDay(it.toString()) }
            )
            Text(
                "Mois de la prochaine échéance",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted
            )
            Text(
                text = state.anchorYearLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (m in 1..12) {
                    val selected = state.anchorMonth == m
                    Surface(
                        color = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.heightIn(min = A11yTokens.minTouchHeight)
                    ) {
                        Text(
                            text = RecurrenceAnchor.frenchMonthLabel(m),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selected) A11yTokens.ButtonText else A11yTokens.Ink,
                            modifier = Modifier
                                .clickable { viewModel.setAnchorMonth(m) }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        )
                    }
                }
            }
            state.error?.let {
                Text(text = it, color = A11yTokens.AccentDanger, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(8.dp))
        }
        PrimaryA11yButton(
            text = if (state.saving) "Enregistrement…" else "Enregistrer",
            onClick = viewModel::save,
            enabled = !state.saving
        )
        if (!keyboardOpen) {
            Spacer(Modifier.height(8.dp))
            DangerA11yButton(
                text = "Retirer cette échéance",
                onClick = { confirmArchive = true },
                enabled = !state.saving
            )
            Spacer(Modifier.height(8.dp))
            SecondaryA11yButton(text = "Annuler", onClick = onCancel)
        }
    }
}
