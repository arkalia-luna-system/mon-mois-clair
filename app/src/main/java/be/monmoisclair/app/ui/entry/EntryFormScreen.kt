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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        recurring = state.recurring,
        showRecurring = true,
        showApplyFollowing = false,
        applyFollowing = false,
        hasTemplate = false,
        error = state.error,
        primaryLabel = if (state.saving) "Enregistrement…" else "Enregistrer",
        onLabel = viewModel::setLabel,
        onAmount = viewModel::setAmount,
        onDay = viewModel::setDay,
        onExpense = viewModel::setExpense,
        onRecurring = viewModel::setRecurring,
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

    EntryFormScaffold(
        title = "Modifier l’entrée",
        label = state.label,
        amountText = state.amountText,
        dayText = state.dayText,
        isExpense = state.isExpense,
        recurring = false,
        showRecurring = false,
        showApplyFollowing = state.hasTemplate,
        applyFollowing = state.applyToFollowing,
        hasTemplate = state.hasTemplate,
        error = state.error,
        primaryLabel = "Enregistrer",
        onLabel = viewModel::setLabel,
        onAmount = viewModel::setAmount,
        onDay = viewModel::setDay,
        onExpense = {},
        onRecurring = {},
        onApplyFollowing = viewModel::setApplyToFollowing,
        onPrimary = viewModel::save,
        onCancel = onCancel,
        onDelete = viewModel::delete
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
    showApplyFollowing: Boolean,
    applyFollowing: Boolean,
    hasTemplate: Boolean,
    error: String?,
    primaryLabel: String,
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
            .verticalScroll(rememberScrollState())
            .padding(A11yTokens.screenPadding),
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

        if (showRecurring) {
            CheckRow(
                checked = recurring,
                label = "Revient tous les mois",
                onChecked = onRecurring
            )
        }
        if (showApplyFollowing && hasTemplate) {
            CheckRow(
                checked = applyFollowing,
                label = "Appliquer aussi aux mois suivants",
                onChecked = onApplyFollowing
            )
        }

        error?.let {
            Text(text = it, color = A11yTokens.AccentDanger, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))
        PrimaryA11yButton(text = primaryLabel, onClick = onPrimary)
        SecondaryA11yButton(text = "Annuler", onClick = onCancel)
        if (onDelete != null) {
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryA11yButton(text = "Supprimer", onClick = onDelete)
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
            .background(bg)
            .clickable(onClick = onClick)
            .heightIn(min = A11yTokens.minTouchHeight)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun CheckRow(checked: Boolean, label: String, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = A11yTokens.minTouchHeight)
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                onValueChange = onChecked
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
