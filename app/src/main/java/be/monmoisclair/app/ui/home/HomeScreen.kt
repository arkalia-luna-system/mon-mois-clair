package be.monmoisclair.app.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.MainActivity
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.components.rememberFocusKeepsVisible
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.imeOpen
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
    val remindersBlocked by viewModel.remindersBlocked.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fit = rememberDisplayFit()
    val keyboardOpen = imeOpen()
    val diversBring = rememberFocusKeepsVisible()
    var diversInput by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val id = (context as? MainActivity)?.consumeHighlightExpenseId()
        if (id != null) viewModel.setHighlightExpenseId(id)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshReminderGate()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
            listState.animateScrollToItem(index)
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
            .imePadding()
    ) {
        // En-tête court : le mois, un chiffre, une ligne. Pas de plaque colorée.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = fit.stickyPaddingHorizontal)
                .padding(top = 4.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthStep(
                    symbol = "‹",
                    description = "Mois précédent",
                    enabled = state.canGoPrev,
                    onClick = viewModel::prevMonth
                )
                Text(
                    text = state.monthLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.InkMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = A11yTokens.minTouchHeight)
                        .clickable(role = Role.Button, onClickLabel = "Ouvrir le menu des mois") {
                            menuOpen = true
                        }
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                )
                MonthStep(
                    symbol = "›",
                    description = "Mois suivant",
                    enabled = state.canGoNext,
                    onClick = viewModel::nextMonth
                )
            }
            Text(
                text = MoneyFormat.formatCents(state.summary.remainingCents),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = fit.remainingAmountSp.sp,
                    lineHeight = (fit.remainingAmountSp + 4f).sp,
                    letterSpacing = (-0.02).em
                ),
                color = when {
                    state.summary.remainingCents < 0L -> A11yTokens.AccentWarn
                    else -> A11yTokens.Ink
                }
            )
            val payLine = buildString {
                append("Reste · à payer ")
                append(MoneyFormat.formatCents(state.summary.stillToPayCents))
                if (state.diversSpentCents > 0L) {
                    append(" · divers ")
                    append(MoneyFormat.formatCents(state.diversSpentCents))
                }
            }
            Text(
                text = payLine,
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    state.summary.stillToPayCents == 0L -> A11yTokens.AccentOk
                    else -> A11yTokens.InkMuted
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (state.summary.remainingCents < 0L &&
                state.timeline.none { it is TimelineItem.Income }
            ) {
                Text(
                    text = "Ajoutez un revenu (salaire).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            state.nextMonthWarning?.let { warning ->
                Text(
                    text = warning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.AccentWarn,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = fit.sectionGap)
                )
            }
        }
        HorizontalDivider(color = A11yTokens.PaperBorder)

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(
                start = fit.screenPadding,
                end = fit.screenPadding,
                top = 12.dp,
                bottom = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)
        ) {
            when {
                state.loading -> {
                    item {
                        Text(
                            text = "Chargement…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = A11yTokens.InkMuted
                        )
                    }
                }
                state.timeline.isEmpty() -> {
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
            if (!state.loading) item(key = "divers-controls") {
                Text(
                    text = "Courses / divers",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Après divers (indicatif) : " +
                        MoneyFormat.formatCents(state.afterDiversCents),
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Boutons AU-DESSUS du champ ; au focus on remonte le bloc au-dessus du clavier.
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = A11yTokens.minTouchHeight)
                        .then(diversBring),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                diversError?.let { err ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = err,
                        color = A11yTokens.AccentDanger,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Zone pouce. Cachée pendant la saisie : le champ divers doit rester visible.
        if (!keyboardOpen) {
        HorizontalDivider(color = A11yTokens.PaperBorder)
        Surface(
            color = A11yTokens.PaperCream,
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
                        text = if (remindersBlocked) "Menu\nAlertes" else "Menu",
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
}

@Composable
private fun MonthStep(
    symbol: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleLarge,
            color = if (enabled) A11yTokens.Ink else A11yTokens.InkMuted
        )
    }
}

@Composable
private fun DayBadge(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = A11yTokens.InkMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.widthIn(min = 56.dp)
    )
}

@Composable
private fun StatusChip(text: String, container: Color, content: Color) {
    Text(
        text = text,
        color = content,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .background(container, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = A11yTokens.minTouchHeight)
                .clickable(onClickLabel = "Modifier", onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DayBadge(item.dayLabel)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.amountLabel,
                    color = A11yTokens.AccentOk,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
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
            DayBadge(item.dayLabel)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClickLabel = "Modifier", onClick = onClick),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.amountLabel,
                    color = if (item.isDone) A11yTokens.InkMuted else A11yTokens.Ink,
                    style = MaterialTheme.typography.bodyLarge
                )
                ExpenseStatusLine(item)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpenseStatusLine(item: TimelineItem.Expense) {
    val showAuto = item.isAutoDebit
    val showDone = item.isDone
    val showLate = item.isOverdue && !item.isDone
    if (!showAuto && !showDone && !showLate) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (showAuto) {
            StatusChip("Auto", A11yTokens.AccentAuto, A11yTokens.Ink)
        }
        if (showDone) {
            StatusChip(item.doneLabel ?: "FAIT ✓", A11yTokens.AccentOk, A11yTokens.ButtonText)
        }
        if (showLate) {
            StatusChip("EN RETARD", A11yTokens.AccentDanger, A11yTokens.ButtonText)
        }
    }
}
