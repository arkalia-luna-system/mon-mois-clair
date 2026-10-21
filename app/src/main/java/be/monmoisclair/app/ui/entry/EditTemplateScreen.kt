package be.monmoisclair.app.ui.entry

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.domain.RecurrenceAnchor
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.components.rememberFocusKeepsVisible
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
    val labelBring = rememberFocusKeepsVisible()
    val amountBring = rememberFocusKeepsVisible()
    var confirmArchive by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }

    fun requestLeave() {
        if (state.edited) confirmLeave = true else onCancel()
    }

    BackHandler { requestLeave() }

    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = A11yTokens.PaperWarm,
            title = {
                Text(
                    text = "Quitter sans enregistrer ?",
                    color = A11yTokens.Ink
                )
            },
            text = {
                Text(
                    text = "Les changements ne seront pas gardés.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Ink
                )
            },
            confirmButton = {
                DangerA11yButton(
                    text = "Quitter",
                    onClick = {
                        confirmLeave = false
                        onCancel()
                    },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            },
            dismissButton = {
                SecondaryA11yButton(
                    text = "Rester",
                    onClick = { confirmLeave = false },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        )
    }

    if (confirmArchive) {
        val name = state.label.trim().ifBlank { "cette échéance" }
        AlertDialog(
            onDismissRequest = { confirmArchive = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = A11yTokens.PaperWarm,
            title = {
                Text(
                    text = "Retirer $name ?",
                    color = A11yTokens.Ink
                )
            },
            text = {
                Text(
                    text = "L’échéance ne reviendra plus les mois suivants. " +
                        "Les mois déjà notés sur l’accueil ne changent pas.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Ink
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
                .background(A11yTokens.PaperCream)
                .padding(A11yTokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        contentDescription = "Chargement de l’échéance"
                        liveRegion = LiveRegionMode.Polite
                    }
            ) {
                CircularProgressIndicator(
                    color = A11yTokens.ButtonFill,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "Chargement…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted
                )
            }
        }
        return
    }

    if (state.missing) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(A11yTokens.PaperCream)
                .padding(A11yTokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Cette échéance n’est plus là.", style = MaterialTheme.typography.headlineMedium)
            val missShape = MaterialTheme.shapes.medium
            Text(
                text = "Elle a peut-être déjà été retirée. Retournez à l’accueil ou aux Réglages.",
                style = MaterialTheme.typography.bodyLarge,
                color = A11yTokens.InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, missShape)
                    .background(A11yTokens.PaperCard, missShape)
                    .padding(14.dp)
            )
            SecondaryA11yButton(text = "Retour", onClick = onCancel)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
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
            val hintShape = MaterialTheme.shapes.medium
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, hintShape)
                    .background(A11yTokens.PaperCard, hintShape)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = state.intervalHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
                )
                Text(
                    text = "Les mois déjà notés sur l’accueil ne changent pas. " +
                        "Les mois suivants suivront cette échéance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
                )
            }
            OutlinedTextField(
                value = state.label,
                onValueChange = viewModel::setLabel,
                label = { Text("Nom") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = A11yTokens.minTouchHeight)
                    .then(labelBring),
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
                    .heightIn(min = A11yTokens.minTouchHeight)
                    .then(amountBring),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            DayOfMonthGrid(
                selectedDay = state.dayText.toIntOrNull(),
                onSelect = { viewModel.setDay(it.toString()) }
            )
            val anchorShape = MaterialTheme.shapes.medium
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.PaperBorder, anchorShape)
                    .background(A11yTokens.PaperCard, anchorShape)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Mois de la prochaine échéance",
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted
                )
                Text(
                    text = state.anchorYearLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Ink
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (m in 1..12) {
                    val selected = state.anchorMonth == m
                    val shape = MaterialTheme.shapes.medium
                    val label = RecurrenceAnchor.frenchMonthLabel(m)
                    Surface(
                        color = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm,
                        shape = shape,
                        modifier = Modifier
                            .heightIn(min = A11yTokens.minTouchHeight)
                            .border(
                                1.dp,
                                if (selected) A11yTokens.ButtonFill else A11yTokens.PaperBorder,
                                shape
                            )
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selected) A11yTokens.ButtonText else A11yTokens.Ink,
                            modifier = Modifier
                                .clickable(
                                    role = Role.RadioButton,
                                    onClickLabel = label
                                ) { viewModel.setAnchorMonth(m) }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                                .semantics {
                                    contentDescription = if (selected) {
                                        "$label, sélectionné"
                                    } else {
                                        label
                                    }
                                }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        state.error?.let {
            val errShape = MaterialTheme.shapes.medium
            Text(
                text = it,
                color = A11yTokens.AccentDanger,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, A11yTokens.AccentDanger.copy(alpha = 0.4f), errShape)
                    .background(A11yTokens.PaperWarm, errShape)
                    .padding(12.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Assertive
                        contentDescription = it
                    }
            )
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
            SecondaryA11yButton(text = "Annuler", onClick = { requestLeave() })
        }
    }
}
