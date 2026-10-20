package be.monmoisclair.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
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
import be.monmoisclair.app.R
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.components.rememberFocusKeepsVisible
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.imeOpen
import be.monmoisclair.app.ui.theme.rememberDisplayFit
import kotlinx.coroutines.launch

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
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var diversInput by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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

    val filteredTimeline = remember(state.timeline, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) state.timeline
        else state.timeline.filter { item ->
            when (item) {
                is TimelineItem.Income -> item.label.contains(q, ignoreCase = true)
                is TimelineItem.Expense ->
                    item.label.contains(q, ignoreCase = true) ||
                        item.paymentNote.contains(q, ignoreCase = true)
            }
        }
    }

    fun onFaitToggle(expenseId: String, markingDone: Boolean) {
        // Mois passé : dialogue d’abord — pas de snackbar anticipé.
        if (state.isPastMonth) {
            viewModel.requestToggleDone(expenseId)
            return
        }
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.requestToggleDone(expenseId)
        if (markingDone) {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Marqué FAIT",
                    actionLabel = "Annuler",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.requestToggleDone(expenseId)
                }
            }
        }
    }

    if (state.pendingPastToggleId != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissPastToggle,
            shape = MaterialTheme.shapes.medium,
            containerColor = A11yTokens.PaperWarm,
            title = { Text("Mois passé", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Vous êtes sur un mois passé (${state.monthLabel}). " +
                        "Voulez-vous vraiment modifier ce paiement ?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                PrimaryA11yButton(
                    text = "Modifier",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.confirmPastToggle()
                    },
                    fillMaxWidth = false,
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            },
            dismissButton = {
                SecondaryA11yButton(
                    text = "Annuler",
                    onClick = viewModel::dismissPastToggle,
                    fillMaxWidth = false,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(A11yTokens.PaperCream)
                .imePadding()
        ) {
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
                        iconRes = R.drawable.ic_chevron_left,
                        description = "Mois précédent",
                        enabled = state.canGoPrev,
                        onClick = viewModel::prevMonth
                    )
                    Text(
                        text = state.monthLabel,
                        style = MaterialTheme.typography.titleLarge,
                        color = A11yTokens.Ink,
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
                        iconRes = R.drawable.ic_chevron_right,
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
                        state.summary.remainingCents < 0L -> A11yTokens.AccentDanger
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
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = A11yTokens.ButtonFill)
                                Text(
                                    text = "Chargement du mois…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = A11yTokens.InkMuted
                                )
                            }
                        }
                    }
                    state.timeline.isEmpty() -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Mois vide",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = A11yTokens.Ink
                                )
                                Text(
                                    text = "Ajoutez le salaire et les factures pour voir ce qu’il reste.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = A11yTokens.InkMuted,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                PrimaryA11yButton(text = "+ Ajouter une entrée", onClick = onAdd)
                            }
                        }
                    }
                    else -> {
                        if (state.timeline.size >= 4) {
                            item(key = "search") {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text("Chercher une facture") },
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = A11yTokens.minTouchHeight),
                                    textStyle = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        items(filteredTimeline, key = { item ->
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
                                    onToggle = { onFaitToggle(item.id, markingDone = !item.isDone) },
                                    onClick = {
                                        viewModel.clearHighlight()
                                        onEdit(item.id, true)
                                    }
                                )
                            }
                        }
                    }
                }
                // Divers seulement quand le mois a déjà des lignes (évite le formulaire orphelin).
                val showDivers = !state.loading &&
                    (state.timeline.isNotEmpty() || state.diversSpentCents > 0L)
                if (showDivers) {
                    item(key = "divers-controls") {
                        Text(
                            text = "Courses / divers",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Après divers (indicatif) : " +
                                MoneyFormat.formatCents(state.afterDiversCents),
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
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
                            shape = MaterialTheme.shapes.medium,
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
            }

            if (!keyboardOpen) {
                HorizontalDivider(color = A11yTokens.PaperBorder)
                Surface(
                    color = A11yTokens.PaperCream,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = A11yTokens.screenPadding,
                                vertical = 8.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (keyboardOpen) 8.dp else 88.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = A11yTokens.Ink,
                contentColor = A11yTokens.ButtonText,
                actionColor = A11yTokens.AccentAuto
            )
        }
    }
}

@Composable
private fun MonthStep(
    iconRes: Int,
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
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (enabled) A11yTokens.Ink else A11yTokens.InkMuted.copy(alpha = 0.4f),
            modifier = Modifier.size(28.dp)
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
            .background(container, MaterialTheme.shapes.medium)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun IncomeRow(item: TimelineItem.Income, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Surface(
        color = A11yTokens.PaperCard,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, A11yTokens.PaperBorder, shape)
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
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.amountLabel,
                    color = A11yTokens.AccentOk,
                    style = MaterialTheme.typography.titleLarge
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
    val shape = MaterialTheme.shapes.medium
    val bg = when {
        item.isDone -> A11yTokens.AccentOkSoft
        item.isOverdue -> A11yTokens.AccentDanger.copy(alpha = 0.22f)
        else -> A11yTokens.PaperCard
    }
    val borderMod = when {
        highlighted -> Modifier.border(3.dp, A11yTokens.ButtonFill, shape)
        item.isOverdue && !item.isDone ->
            Modifier.border(2.dp, A11yTokens.AccentDanger, shape)
        else -> Modifier.border(1.dp, A11yTokens.PaperBorder, shape)
    }
    Surface(
        color = bg,
        shape = shape,
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
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.amountLabel,
                    color = if (item.isDone) A11yTokens.InkMuted else A11yTokens.Ink,
                    style = MaterialTheme.typography.titleLarge
                )
                AnimatedVisibility(
                    visible = item.paymentNote.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = item.paymentNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.InkMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                ExpenseStatusLine(item)
            }
            SecondaryA11yButton(
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
    val showReceipt = item.hasReceipt
    if (!showAuto && !showDone && !showLate && !showReceipt) return
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
        if (showReceipt) {
            StatusChip("Photo", A11yTokens.PaperWarm, A11yTokens.Ink)
        }
    }
}
