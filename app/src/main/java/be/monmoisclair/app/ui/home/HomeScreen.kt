package be.monmoisclair.app.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.MainActivity
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.rememberDisplayFit

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
    val fit = rememberDisplayFit()
    var diversInput by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }

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

    if (menuOpen) {
        HomeMenuSheet(
            selectedYearMonth = state.selectedYearMonth,
            isPastMonth = state.isPastMonth,
            onDismiss = { menuOpen = false },
            onSelectMonth = viewModel::goToMonth,
            onGoToCurrentMonth = viewModel::goToCurrentMonth,
            onOpenHealth = onOpenHealth,
            onOpenSettings = onOpenSettings
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A11yTokens.PaperCream)
    ) {
        // Zone lecture (~30 %) — montants, pas de boutons critiques hors ‹ › mois.
        Surface(
            color = A11yTokens.PaperWarm,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = fit.stickyPaddingHorizontal,
                    vertical = fit.stickyPaddingVertical
                )
            ) {
                Text(
                    text = "Mon Mois Clair",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = fit.brandTitleSp.sp
                    ),
                    color = A11yTokens.InkMuted
                )
                Text(
                    text = "Reste après factures",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = MoneyFormat.formatCents(state.summary.remainingCents),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = fit.remainingAmountSp.sp
                    ),
                    color = when {
                        state.summary.remainingCents < 0L -> A11yTokens.AccentWarn
                        else -> A11yTokens.Ink
                    }
                )
                Text(
                    text = "À payer : " +
                        MoneyFormat.formatCents(state.summary.stillToPayCents) +
                        " · Divers : " +
                        MoneyFormat.formatCents(state.diversSpentCents),
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        state.summary.stillToPayCents == 0L -> A11yTokens.AccentOk
                        else -> A11yTokens.Ink
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (state.summary.remainingCents < 0L &&
                    state.timeline.none { it is TimelineItem.Income }
                ) {
                    Text(
                        text = "Astuce : ajoutez un revenu (salaire).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted
                    )
                }
                Spacer(modifier = Modifier.height(fit.sectionGap))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "‹",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (state.canGoPrev) A11yTokens.Ink else A11yTokens.InkMuted,
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
                            .heightIn(min = A11yTokens.minTouchHeight)
                            .clickable { menuOpen = true }
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (state.canGoNext) A11yTokens.Ink else A11yTokens.InkMuted,
                        modifier = Modifier
                            .heightIn(min = A11yTokens.minTouchHeight)
                            .clickable(enabled = state.canGoNext) { viewModel.nextMonth() }
                            .padding(8.dp)
                    )
                }
                state.nextMonthWarning?.let { warning ->
                    Spacer(modifier = Modifier.height(fit.sectionGap))
                    Text(
                        text = warning,
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.AccentWarn,
                        maxLines = if (fit.compactHomeChrome) 3 else 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(A11yTokens.PaperCream, RoundedCornerShape(10.dp))
                            .padding(fit.bannerPadding)
                    )
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(
                start = fit.screenPadding,
                end = fit.screenPadding,
                top = fit.screenPadding,
                bottom = fit.screenPadding
            ),
            verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)
        ) {
            item(key = "divers-controls") {
                Text(
                    text = "Après divers (indicatif) : " +
                        MoneyFormat.formatCents(state.afterDiversCents),
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
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
                        text = "Touchez « + Ajouter » en bas.",
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
        }

        // Zone pouce — barre fixe (étude One UI / thumb zone).
        HorizontalDivider(color = A11yTokens.PaperBorder)
        Surface(
            color = A11yTokens.PaperWarm,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // weight sur Box : évite que le hit-target Menu mange tout l’écran.
                Box(modifier = Modifier.weight(1f)) {
                    SecondaryA11yButton(
                        text = "Menu",
                        onClick = { menuOpen = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1.2f)) {
                    PrimaryA11yButton(
                        text = "+ Ajouter",
                        onClick = onAdd,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeRow(item: TimelineItem.Income, onClick: () -> Unit) {
    Surface(
        color = A11yTokens.PaperCard,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, A11yTokens.PaperBorder, RoundedCornerShape(10.dp))
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
        item.isDone -> A11yTokens.AccentOkSoft
        item.isOverdue -> A11yTokens.AccentDanger.copy(alpha = 0.12f)
        else -> A11yTokens.PaperCard
    }
    val borderMod = when {
        highlighted -> Modifier.border(3.dp, A11yTokens.ButtonFill, RoundedCornerShape(10.dp))
        item.isOverdue && !item.isDone ->
            Modifier.border(2.dp, A11yTokens.AccentDanger, RoundedCornerShape(10.dp))
        else -> Modifier.border(1.dp, A11yTokens.PaperBorder, RoundedCornerShape(10.dp))
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(borderMod)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
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
                                color = A11yTokens.AccentAuto,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        when {
                            item.isDone -> Text(
                                text = item.doneLabel ?: "FAIT ✓",
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
            PrimaryA11yButton(
                text = if (item.isDone) "Remettre" else "FAIT ✓",
                onClick = onToggle,
                fillMaxWidth = false,
                modifier = Modifier.widthIn(min = 96.dp)
            )
        }
    }
}
