package be.monmoisclair.app.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.ui.components.DangerA11yButton
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.theme.A11yTokens

@Composable
fun AddEntryScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AddEntryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    EntryFormScaffold(
        title = "Nouvelle entrée",
        label = state.label,
        amountText = state.amountText,
        dayText = state.dayText,
        isExpense = state.isExpense,
        recurring = state.recurrence != RecurrenceChoice.Once,
        showRecurring = true,
        recurrence = state.recurrence,
        onRecurrence = viewModel::setRecurrence,
        nextDueMonth = state.nextDueMonth,
        onNextDueMonth = viewModel::setNextDueMonth,
        showNextDueMonth = state.needsNextDueMonth,
        isAutoDebit = state.isAutoDebit,
        onAutoDebit = viewModel::setAutoDebit,
        showAutoDebit = state.isExpense,
        showApplyFollowing = false,
        applyFollowing = false,
        hasTemplate = false,
        error = state.error,
        primaryLabel = if (state.saving) "Enregistrement…" else "Enregistrer",
        primaryEnabled = !state.saving,
        onLabel = viewModel::setLabel,
        onAmount = viewModel::setAmount,
        onDay = viewModel::setDay,
        onExpense = viewModel::setExpense,
        onRecurring = { on ->
            viewModel.setRecurrence(
                if (on) RecurrenceChoice.Monthly else RecurrenceChoice.Once
            )
        },
        onApplyFollowing = {},
        onPrimary = viewModel::save,
        onCancel = onCancel,
        onDelete = null
    )
}

@Composable
fun EditEntryScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    viewModel: EditEntryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    LaunchedEffect(state.done) { if (state.done) onDone() }

    if (state.loading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(A11yTokens.PaperCream)
                .padding(A11yTokens.screenPadding)
        ) {
            Text("Chargement…", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = A11yTokens.PaperWarm,
            title = { Text("Supprimer cette entrée ?") },
            text = {
                Text(
                    if (state.applyToFollowing && state.hasTemplate) {
                        "La ligne sera effacée, le modèle désactivé, et les mois suivants encore « à payer » seront aussi supprimés."
                    } else {
                        "Cette action retire la ligne du mois. Vous pourrez en recréer une ensuite."
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        viewModel.delete()
                    }
                ) {
                    Text("Supprimer", color = A11yTokens.AccentDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    EntryFormScaffold(
        title = "Modifier l’entrée",
        label = state.label,
        amountText = state.amountText,
        dayText = state.dayText,
        isExpense = state.isExpense,
        recurring = false,
        showRecurring = false,
        isAutoDebit = state.isAutoDebit,
        onAutoDebit = viewModel::setAutoDebit,
        showAutoDebit = state.isExpense,
        showApplyFollowing = state.hasTemplate,
        applyFollowing = state.applyToFollowing,
        hasTemplate = state.hasTemplate,
        error = state.error,
        primaryLabel = "Enregistrer",
        primaryEnabled = true,
        onLabel = viewModel::setLabel,
        onAmount = viewModel::setAmount,
        onDay = viewModel::setDay,
        onExpense = {},
        onRecurring = {},
        onApplyFollowing = viewModel::setApplyToFollowing,
        onPrimary = viewModel::save,
        onCancel = onCancel,
        onDelete = { confirmDelete = true }
    )
}

@Composable
private fun EntryFormScaffold(
    title: String,
    label: String,
    amountText: String,
    dayText: String,
    isExpense: Boolean,
    recurring: Boolean,
    showRecurring: Boolean,
    recurrence: RecurrenceChoice = RecurrenceChoice.Once,
    onRecurrence: ((RecurrenceChoice) -> Unit)? = null,
    nextDueMonth: Int = 1,
    onNextDueMonth: (Int) -> Unit = {},
    showNextDueMonth: Boolean = false,
    isAutoDebit: Boolean = false,
    onAutoDebit: (Boolean) -> Unit = {},
    showAutoDebit: Boolean = false,
    showApplyFollowing: Boolean,
    applyFollowing: Boolean,
    hasTemplate: Boolean,
    error: String?,
    primaryLabel: String,
    primaryEnabled: Boolean,
    onLabel: (String) -> Unit,
    onAmount: (String) -> Unit,
    onDay: (String) -> Unit,
    onExpense: (Boolean) -> Unit,
    onRecurring: (Boolean) -> Unit,
    onApplyFollowing: (Boolean) -> Unit,
    onPrimary: () -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)?
) {
    // Champs scrollables + actions épinglées au-dessus du clavier (imePadding).
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
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)

            if (showRecurring) {
                Text("Type", style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TypeChip("Dépense (−)", selected = isExpense) { onExpense(true) }
                    TypeChip("Revenu (+)", selected = !isExpense) { onExpense(false) }
                }
            }

            OutlinedTextField(
                value = label,
                onValueChange = onLabel,
                label = { Text("Nom (ex. Loyer)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = A11yTokens.minTouchHeight),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = onAmount,
                label = { Text("Montant (€)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = A11yTokens.minTouchHeight),
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = dayText,
                onValueChange = onDay,
                label = { Text("Jour du mois (1–31)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = A11yTokens.minTouchHeight),
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            if (showRecurring && onRecurrence != null) {
                Text("Fréquence", style = MaterialTheme.typography.titleLarge)
                TypeChip("Une seule fois", selected = recurrence == RecurrenceChoice.Once) {
                    onRecurrence(RecurrenceChoice.Once)
                }
                TypeChip("Tous les mois", selected = recurrence == RecurrenceChoice.Monthly) {
                    onRecurrence(RecurrenceChoice.Monthly)
                }
                TypeChip("Tous les 3 mois", selected = recurrence == RecurrenceChoice.Every3Months) {
                    onRecurrence(RecurrenceChoice.Every3Months)
                }
                TypeChip("1× par an", selected = recurrence == RecurrenceChoice.Yearly) {
                    onRecurrence(RecurrenceChoice.Yearly)
                }
                if (showNextDueMonth) {
                    Text("Prochaine échéance", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Choisissez le mois où elle tombe (ex. taxe en mars).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted
                    )
                    for (m in 1..12) {
                        TypeChip(
                            text = be.monmoisclair.app.domain.RecurrenceAnchor.frenchMonthLabel(m),
                            selected = nextDueMonth == m
                        ) {
                            onNextDueMonth(m)
                        }
                    }
                }
            } else if (showRecurring) {
                CheckRow(
                    checked = recurring,
                    label = "Revient tous les mois",
                    onChecked = onRecurring
                )
            }
            if (showAutoDebit) {
                CheckRow(
                    checked = isAutoDebit,
                    label = "Prélèvement automatique",
                    onChecked = onAutoDebit
                )
                Text(
                    text = "Les rappels diront de vérifier le prélèvement (la ligne reste à cocher FAIT).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
                )
            }
            if (showApplyFollowing && hasTemplate) {
                CheckRow(
                    checked = applyFollowing,
                    label = "Appliquer aussi aux mois suivants (encore à payer)",
                    onChecked = onApplyFollowing
                )
            }

            error?.let {
                Text(text = it, color = A11yTokens.AccentDanger, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        PrimaryA11yButton(text = primaryLabel, onClick = onPrimary, enabled = primaryEnabled)
        Spacer(modifier = Modifier.height(8.dp))
        SecondaryA11yButton(text = "Annuler", onClick = onCancel)
        if (onDelete != null) {
            Spacer(modifier = Modifier.height(8.dp))
            DangerA11yButton(text = "Supprimer", onClick = onDelete)
        }
    }
}

@Composable
private fun TypeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) A11yTokens.ButtonFill else A11yTokens.PaperWarm
    val fg = if (selected) A11yTokens.ButtonText else A11yTokens.Ink
    Text(
        text = text,
        color = fg,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .heightIn(min = A11yTokens.minTouchHeight)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun CheckRow(checked: Boolean, label: String, onChecked: (Boolean) -> Unit) {
    // Checkbox avec onCheckedChange réel : le pattern toggleable+null
    // n’enregistre pas les taps sur One UI / uiautomator (case « Revient… » morte).
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .clickable(role = Role.Checkbox) { onChecked(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
