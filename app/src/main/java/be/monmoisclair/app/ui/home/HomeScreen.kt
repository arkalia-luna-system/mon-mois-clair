package be.monmoisclair.app.ui.home

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import java.time.YearMonth
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onAdd: (YearMonth) -> Unit,
    onEdit: (id: String, isExpense: Boolean) -> Unit,
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
    var diversExpanded by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    /** S02 : ne pas chasser le snackbar « Annuler » FAIT. */
    var faitUndoActive by remember { mutableStateOf(false) }
    var receiptPathToShow by remember { mutableStateOf<String?>(null) }
    var receiptTitle by remember { mutableStateOf("Facture") }
    var receiptBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var receiptLoading by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        viewModel.userMessages.collect { msg ->
            // Pendant « Annuler » FAIT : ignorer « Copié » (sinon le bandeau disparaît).
            if (faitUndoActive) return@collect
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(receiptPathToShow) {
        val path = receiptPathToShow
        if (path.isNullOrBlank()) {
            receiptBitmap = null
            receiptLoading = false
            return@LaunchedEffect
        }
        receiptLoading = true
        receiptBitmap = null
        val bmp = viewModel.loadReceiptBitmap(path)
        receiptBitmap = bmp
        receiptLoading = false
        // Garde le dialogue ouvert même si le fichier manque (message dans le dialogue).
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
    val searchFieldVisible = !state.loading && state.timeline.size >= 4

    // P3-F02 : scroller dans la liste filtrée (+ offset du champ recherche).
    LaunchedEffect(state.highlightExpenseId, filteredTimeline, searchQuery, searchFieldVisible) {
        val id = state.highlightExpenseId ?: return@LaunchedEffect
        var index = filteredTimeline.indexOfFirst {
            it is TimelineItem.Expense && it.id == id
        }
        if (index < 0 && searchQuery.isNotBlank()) {
            searchQuery = ""
            return@LaunchedEffect
        }
        if (index < 0) return@LaunchedEffect
        if (searchFieldVisible) index += 1
        listState.animateScrollToItem(index)
    }

    var pastMarkingDone by remember { mutableStateOf(true) }

    fun onFaitToggle(expenseId: String, markingDone: Boolean) {
        // Mois passé : dialogue d’abord — pas de snackbar anticipé.
        if (state.isPastMonth) {
            pastMarkingDone = markingDone
            viewModel.requestToggleDone(expenseId)
            return
        }
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.requestToggleDone(expenseId)
        scope.launch {
            faitUndoActive = true
            try {
                val result = snackbarHostState.showSnackbar(
                    message = if (markingDone) "Marqué FAIT" else "Remis à payer",
                    actionLabel = "Annuler",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.requestToggleDone(expenseId)
                }
            } finally {
                faitUndoActive = false
            }
        }
    }

    if (receiptPathToShow != null) {
        Dialog(
            onDismissRequest = { receiptPathToShow = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = A11yTokens.PaperCream,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(A11yTokens.screenPadding)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = receiptTitle,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    when {
                        receiptLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = A11yTokens.ButtonFill)
                            }
                        }
                        receiptBitmap != null -> {
                            Image(
                                bitmap = receiptBitmap!!.asImageBitmap(),
                                contentDescription = "Photo de la facture",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 280.dp, max = 520.dp)
                                    .background(A11yTokens.PaperWarm, MaterialTheme.shapes.medium)
                            )
                        }
                        else -> {
                            Text(
                                text = "Photo introuvable dans Documents. " +
                                    "Ouvrez la ligne pour en reprendre une.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = A11yTokens.AccentDanger
                            )
                        }
                    }
                    PrimaryA11yButton(
                        text = "Fermer",
                        onClick = { receiptPathToShow = null }
                    )
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
                    text = if (pastMarkingDone) "Marquer FAIT" else "Remettre à payer",
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
                    text = if (state.summary.remainingCents < 0L) {
                        "Déficit après factures"
                    } else {
                        "Reste après factures"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
                )
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
                stickyStillToPayLabel(
                    stillToPayCents = state.summary.stillToPayCents,
                    hasExpenses = state.timeline.any { it is TimelineItem.Expense }
                )?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = when {
                            state.summary.stillToPayCents == 0L -> A11yTokens.AccentOk
                            else -> A11yTokens.InkMuted
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                stickyWithoutUnpaidLabel(
                    remainingCents = state.summary.remainingCents,
                    stillToPayCents = state.summary.stillToPayCents
                )?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.AccentWarn,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (state.diversSpentCents > 0L) {
                    Text(
                        text = "Après courses ${MoneyFormat.formatCents(state.afterDiversCents)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
                        modifier = Modifier
                            .padding(top = fit.sectionGap)
                            .fillMaxWidth()
                            .heightIn(min = A11yTokens.minTouchHeight)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Voir les échéances hors ce mois"
                            ) { onOpenSettings() }
                            .padding(vertical = 8.dp)
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
                                    text = "Ajoutez le salaire et les factures avec « + Ajouter » en bas.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = A11yTokens.InkMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        if (searchFieldVisible) {
                            item(key = "search") {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        label = { Text("Chercher une facture") },
                                        singleLine = true,
                                        shape = MaterialTheme.shapes.medium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = A11yTokens.minTouchHeight),
                                        textStyle = MaterialTheme.typography.bodyLarge,
                                        trailingIcon = if (searchQuery.isNotEmpty()) {
                                            {
                                                Text(
                                                    text = "Effacer",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = A11yTokens.ButtonFill,
                                                    modifier = Modifier
                                                        .heightIn(min = A11yTokens.minTouchHeight)
                                                        .clickable(
                                                            role = Role.Button,
                                                            onClick = { searchQuery = "" }
                                                        )
                                                        .padding(horizontal = 8.dp)
                                                )
                                            }
                                        } else {
                                            null
                                        }
                                    )
                                    if (searchQuery.isNotBlank()) {
                                        Text(
                                            text = "Le reste en haut est celui de tout le mois.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = A11yTokens.InkMuted
                                        )
                                    }
                                }
                            }
                        }
                        if (searchQuery.isNotBlank() && filteredTimeline.isEmpty()) {
                            item(key = "search-empty") {
                                Text(
                                    text = "Rien pour « ${searchQuery.trim()} ». " +
                                        "Les totaux du haut restent ceux du mois.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = A11yTokens.InkMuted,
                                    modifier = Modifier.padding(vertical = 12.dp)
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
                                    },
                                    onCopyNote = {
                                        viewModel.copyPaymentNote(item.paymentNote)
                                    },
                                    onOpenReceipt = {
                                        val path = item.receiptRelativePath.trim()
                                        if (path.isEmpty()) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Aucune photo liée à cette facture."
                                                )
                                            }
                                        } else {
                                            receiptTitle = item.label.trim().ifBlank { "Facture" }
                                            receiptPathToShow = path
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                // Divers : total visible fermé ; formulaire au tap.
                val showDivers = !state.loading &&
                    (state.timeline.isNotEmpty() || state.diversSpentCents > 0L)
                if (showDivers) {
                    item(key = "divers-controls") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = A11yTokens.minTouchHeight)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = if (diversExpanded) {
                                        "Fermer courses"
                                    } else {
                                        "Ouvrir courses"
                                    }
                                ) { diversExpanded = !diversExpanded }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (diversExpanded) {
                                    "Courses"
                                } else {
                                    "Noter des courses"
                                },
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = if (diversExpanded) "Fermer" else "Ouvrir",
                                style = MaterialTheme.typography.bodyLarge,
                                color = A11yTokens.ButtonFill
                            )
                        }
                        Text(
                            text = if (state.diversSpentCents > 0L) {
                                "Après courses ${MoneyFormat.formatCents(state.afterDiversCents)}"
                            } else {
                                "Aucune course notée."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted
                        )
                        AnimatedVisibility(visible = diversExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SecondaryA11yButton(
                                        text = "Retirer",
                                        onClick = { viewModel.removeDivers(diversInput) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    SecondaryA11yButton(
                                        text = "Ajouter",
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
                                    label = { Text("Montant (€)") },
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.medium,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    ),
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
                                text = if (remindersBlocked) "Menu · Alertes" else "Menu",
                                onClick = { menuOpen = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1.2f)) {
                            PrimaryA11yButton(
                                text = "+ Ajouter",
                                onClick = { onAdd(state.selectedYearMonth) },
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
                actionColor = A11yTokens.ButtonText
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
private fun StatusChip(
    text: String,
    container: Color,
    content: Color
) {
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
                    color = A11yTokens.Ink,
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
    onClick: () -> Unit,
    onCopyNote: () -> Unit,
    onOpenReceipt: () -> Unit
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = item.paymentNote,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = A11yTokens.InkMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                SecondaryA11yButton(
                                    text = "Copier",
                                    onClick = onCopyNote,
                                    fillMaxWidth = false,
                                    modifier = Modifier.widthIn(min = 96.dp)
                                )
                            }
                        }
                    }
                }
                // Hors zone « Modifier » : sinon le tap ouvre l’édition au lieu de la photo.
                ExpenseStatusLine(item = item, onOpenReceipt = onOpenReceipt)
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
private fun ExpenseStatusLine(
    item: TimelineItem.Expense,
    onOpenReceipt: () -> Unit
) {
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
            StatusChip("Prélèvement", A11yTokens.AccentAuto, A11yTokens.Ink)
        }
        if (showDone) {
            StatusChip(item.doneLabel ?: "FAIT ✓", A11yTokens.AccentOk, A11yTokens.ButtonText)
        }
        if (showLate) {
            StatusChip("En retard", A11yTokens.AccentDanger, A11yTokens.ButtonText)
        }
        if (showReceipt) {
            // Bouton réel (pas pastille Text) : le tap ouvre bien la photo.
            SecondaryA11yButton(
                text = "Voir la photo",
                onClick = onOpenReceipt,
                fillMaxWidth = false,
                modifier = Modifier.widthIn(min = 96.dp)
            )
        }
    }
}
