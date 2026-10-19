package be.monmoisclair.app.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.MainActivity
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens

@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    onEdit: (id: String, isExpense: Boolean) -> Unit,
    onOpenHealth: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val diversError by viewModel.diversError.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var diversInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val id = (context as? MainActivity)?.consumeHighlightExpenseId()
        if (id != null) viewModel.setHighlightExpenseId(id)
    }

    LaunchedEffect(Unit) {
        viewModel.diversFieldCleared.collect { diversInput = "" }
    }

    LaunchedEffect(state.highlightExpenseId, state.timeline) {
        val id = state.highlightExpenseId ?: return@LaunchedEffect
        val index = state.timeline.indexOfFirst {
            it is TimelineItem.Expense && it.id == id
        }
        if (index >= 0) {
            // +1 : item « divers-controls » avant la timeline
            listState.animateScrollToItem(index + 1)
        }
    }

    if (state.pendingPastToggleId != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissPastToggle,
            containerColor = A11yTokens.PaperWarm,
            title = { Text("Mois passé") },
            text = {
                Text(
                    "Vous êtes sur un mois passé (${state.monthLabel}). " +
                        "Voulez-vous vraiment modifier ce paiement ?"
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmPastToggle) {
                    Text("Modifier")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissPastToggle) {
                    Text("Annuler")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
    ) {
        Surface(
            color = A11yTokens.PaperWarm,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(A11yTokens.screenPadding)) {
                Text(text = "Mon Mois Clair", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reste après factures",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Pas le solde banque — revenus moins factures du mois.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
                )
                Text(
                    text = MoneyFormat.formatCents(state.summary.remainingCents),
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "Encore à payer : " +
                        MoneyFormat.formatCents(state.summary.stillToPayCents),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Courses / divers : " + MoneyFormat.formatCents(state.diversSpentCents),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Après divers (indicatif) : " +
                        MoneyFormat.formatCents(state.afterDiversCents),
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "‹ Préc.",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (state.canGoPrev) A11yTokens.Ink else A11yTokens.InkMuted,
                        softWrap = false,
                        modifier = Modifier
                            .heightIn(min = A11yTokens.minTouchHeight)
                            .clickable(enabled = state.canGoPrev) { viewModel.prevMonth() }
                            .padding(8.dp)
                    )
                    Text(
                        text = state.monthLabel,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "Suiv. ›",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (state.canGoNext) A11yTokens.Ink else A11yTokens.InkMuted,
                        softWrap = false,
                        modifier = Modifier
                            .heightIn(min = A11yTokens.minTouchHeight)
                            .clickable(enabled = state.canGoNext) { viewModel.nextMonth() }
                            .padding(8.dp)
                    )
                }
                if (!state.canGoNext) {
                    Text(
                        text = "Mois en cours : pas de mois suivant.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted
                    )
                }
                state.nextMonthWarning?.let { warning ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = warning,
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.AccentWarn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(A11yTokens.PaperWarm, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    )
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(
                start = A11yTokens.screenPadding,
                end = A11yTokens.screenPadding,
                top = A11yTokens.screenPadding,
                bottom = A11yTokens.screenPadding + A11yTokens.bottomSystemClearance
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Saisie divers dans le scroll : le sticky garde le reste lisible + la liste visible.
            item(key = "divers-controls") {
                Text(
                    text = "Astuce : « Marquer FAIT » ne supprime pas la ligne — elle devient verte.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = diversInput,
                    onValueChange = {
                        diversInput = it
                        viewModel.clearDiversError()
                    },
                    label = { Text("Montant divers (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().heightIn(min = A11yTokens.minTouchHeight),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecondaryA11yButton(
                        text = "− Divers",
                        onClick = { viewModel.removeDivers(diversInput) },
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryA11yButton(
                        text = "+ Divers",
                        onClick = { viewModel.addDivers(diversInput) },
                        modifier = Modifier.weight(1f)
                    )
                }
                diversError?.let { err ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = err,
                        color = A11yTokens.AccentDanger,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            if (state.timeline.isEmpty() && !state.loading) {
                item {
                    Text(
                        text = "Aucune entrée ce mois-ci.",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Touchez « Ajouter une entrée » plus bas pour un salaire, un loyer, etc.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            items(state.timeline, key = { item ->
                when (item) {
                    is TimelineItem.Income -> "i-${item.id}"
                    is TimelineItem.Expense -> "e-${item.id}"
                }
            }) { item ->
                when (item) {
                    is TimelineItem.Income -> IncomeRow(
                        item = item,
                        onClick = { onEdit(item.id, false) }
                    )
                    is TimelineItem.Expense -> ExpenseRow(
                        item = item,
                        highlighted = item.id == state.highlightExpenseId,
                        onToggle = { viewModel.requestToggleDone(item.id) },
                        onClick = {
                            viewModel.clearHighlight()
                            onEdit(item.id, true)
                        }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryA11yButton(text = "Ajouter une entrée", onClick = onAdd)
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryA11yButton(text = "Vérifier les alertes", onClick = onOpenHealth)
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryA11yButton(text = "Réglages", onClick = onOpenSettings)
            }
        }
    }
}

@Composable
private fun IncomeRow(item: TimelineItem.Income, onClick: () -> Unit) {
    Surface(
        color = A11yTokens.PaperWarm,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = A11yTokens.minTouchHeight)
                .clickable(onClick = onClick)
                .padding(14.dp)
        ) {
            Text(text = item.dayLabel, style = MaterialTheme.typography.bodyMedium)
            Text(text = item.label, style = MaterialTheme.typography.titleLarge)
            Text(
                text = item.amountLabel,
                color = A11yTokens.AccentOk,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ExpenseRow(
    item: TimelineItem.Expense,
    highlighted: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val bg = when {
        item.isDone -> A11yTokens.AccentOk.copy(alpha = 0.18f)
        item.isOverdue -> A11yTokens.AccentDanger.copy(alpha = 0.12f)
        else -> A11yTokens.PaperWarm
    }
    val borderMod = when {
        highlighted -> Modifier.border(3.dp, A11yTokens.ButtonFill, RoundedCornerShape(8.dp))
        item.isOverdue && !item.isDone ->
            Modifier.border(2.dp, A11yTokens.AccentDanger, RoundedCornerShape(8.dp))
        else -> Modifier
    }
    Surface(
        color = bg,
        modifier = Modifier
            .fillMaxWidth()
            .then(borderMod)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = item.dayLabel, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (item.isAutoDebit) {
                            Text(
                                text = "Auto",
                                color = A11yTokens.InkMuted,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        when {
                            item.isDone -> Text(
                                text = "FAIT ✓",
                                color = A11yTokens.AccentOk,
                                style = MaterialTheme.typography.labelLarge
                            )
                            item.isOverdue -> Text(
                                text = "EN RETARD",
                                color = A11yTokens.AccentDanger,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                Text(text = item.label, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = item.amountLabel,
                    color = if (item.isDone) A11yTokens.InkMuted else A11yTokens.Ink,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryA11yButton(
                text = if (item.isDone) "Remettre à payer" else "Marquer FAIT ✓",
                onClick = onToggle
            )
        }
    }
}
