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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
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
import be.monmoisclair.app.domain.BelgianStructuredCommunication
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
    onOpenHealth: () -> Unit = {},
    onOpenDossiers: () -> Unit = {},
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
                        item.causeLabel.contains(q, ignoreCase = true) ||
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
                    message = if (markingDone) "Noté FAIT" else "Remis à payer",
                    actionLabel = "Annuler",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoToggleDone(expenseId)
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
                        color = A11yTokens.Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    when {
                        receiptLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(A11yTokens.PaperWarm, MaterialTheme.shapes.medium)
                                    .semantics {
                                        contentDescription = "Chargement de la photo"
                                        liveRegion = LiveRegionMode.Polite
                                    },
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
                            val photoErrShape = MaterialTheme.shapes.medium
                            Text(
                                text = "Impossible d’ouvrir cette photo. " +
                                    "Ouvrez la ligne pour en reprendre une.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = A11yTokens.AccentDanger,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        A11yTokens.AccentDanger.copy(alpha = 0.4f),
                                        photoErrShape
                                    )
                                    .background(A11yTokens.PaperWarm, photoErrShape)
                                    .padding(12.dp)
                                    .semantics {
                                        liveRegion = LiveRegionMode.Assertive
                                        contentDescription =
                                            "Impossible d’ouvrir cette photo. " +
                                                "Ouvrez la ligne pour en reprendre une."
                                    }
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
            title = {
                Text(
                    text = "Mois passé",
                    style = MaterialTheme.typography.titleLarge,
                    color = A11yTokens.Ink
                )
            },
            text = {
                Text(
                    text = if (pastMarkingDone) {
                        "Vous êtes sur un mois passé (${state.monthLabel}). " +
                            "Marquer cette ligne FAIT ?"
                    } else {
                        "Vous êtes sur un mois passé (${state.monthLabel}). " +
                            "Remettre cette ligne à payer ?"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = A11yTokens.Ink
                )
            },
            confirmButton = {
                PrimaryA11yButton(
                    text = if (pastMarkingDone) "Marquer FAIT" else "Remettre à payer",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        val id = state.pendingPastToggleId
                        viewModel.confirmPastToggle()
                        if (id != null) {
                            scope.launch {
                                faitUndoActive = true
                                try {
                                    val result = snackbarHostState.showSnackbar(
                                        message = if (pastMarkingDone) {
                                            "Noté FAIT"
                                        } else {
                                            "Remis à payer"
                                        },
                                        actionLabel = "Annuler",
                                        duration = SnackbarDuration.Long
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoToggleDone(id)
                                    }
                                } finally {
                                    faitUndoActive = false
                                }
                            }
                        }
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
            onOpenSettings = onOpenSettings,
            dossierCount = state.dossierCount,
            pendingLetterCount = state.pendingLetterCount,
            onOpenDossiers = onOpenDossiers
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
                    .padding(vertical = fit.stickyPaddingVertical)
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
                val remainingTitle = if (state.summary.remainingCents < 0L) {
                    "Déficit après factures"
                } else {
                    "Reste après factures"
                }
                val remainingAmount = MoneyFormat.formatCents(state.summary.remainingCents)
                Text(
                    text = remainingTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = A11yTokens.InkMuted
                )
                Text(
                    text = remainingAmount,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = fit.remainingAmountSp.sp,
                        lineHeight = (fit.remainingAmountSp + 4f).sp,
                        letterSpacing = (-0.02).em
                    ),
                    color = when {
                        state.summary.remainingCents < 0L -> A11yTokens.AccentDanger
                        else -> A11yTokens.Ink
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "$remainingTitle : $remainingAmount"
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
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { contentDescription = label }
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
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { contentDescription = label }
                    )
                }
                if (state.diversSpentCents > 0L) {
                    val afterLabel =
                        "Après courses ${MoneyFormat.formatCents(state.afterDiversCents)}"
                    Text(
                        text = afterLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = A11yTokens.InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { contentDescription = afterLabel }
                    )
                }
                if (state.summary.remainingCents < 0L &&
                    state.timeline.none { it is TimelineItem.Income }
                ) {
                    val incomeTipShape = MaterialTheme.shapes.medium
                    Text(
                        text = "Ajoutez un revenu (salaire).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.AccentWarn,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                A11yTokens.AccentWarn.copy(alpha = 0.45f),
                                incomeTipShape
                            )
                            .background(A11yTokens.PaperWarm, incomeTipShape)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = "Ajoutez un revenu (salaire)."
                            }
                    )
                }
                state.nextMonthWarning?.let { warning ->
                    val warnShape = MaterialTheme.shapes.medium
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
                            .border(1.dp, A11yTokens.AccentWarn.copy(alpha = 0.45f), warnShape)
                            .background(A11yTokens.PaperWarm, warnShape)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Voir les échéances hors ce mois"
                            ) { onOpenSettings() }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = warning
                            }
                    )
                }
                if (state.pendingLetterCount > 0) {
                    val pendingLabel = if (state.pendingLetterCount == 1) {
                        "1 courrier sans plan · Voir"
                    } else {
                        "${state.pendingLetterCount} courriers sans plan · Voir"
                    }
                    val pendingShape = MaterialTheme.shapes.medium
                    Text(
                        text = pendingLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = fit.sectionGap)
                            .fillMaxWidth()
                            .heightIn(min = A11yTokens.minTouchHeight)
                            .border(1.dp, A11yTokens.PaperBorder, pendingShape)
                            .background(A11yTokens.PaperWarm, pendingShape)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Ouvrir Mes dossiers"
                            ) { onOpenDossiers() }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = pendingLabel
                            }
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
                    top = if (fit.compactHomeChrome) 8.dp else 12.dp,
                    bottom = if (fit.compactHomeChrome) 8.dp else 12.dp
                ),
                // Écart cartes : 12 dp (charte) même en compact — faux clics.
                verticalArrangement = Arrangement.spacedBy(A11yTokens.cardGap)
            ) {
                when {
                    state.loading -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp)
                                    .semantics {
                                        contentDescription = "Chargement du mois"
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val loadShape = MaterialTheme.shapes.medium
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, A11yTokens.PaperBorder, loadShape)
                                        .background(A11yTokens.PaperCard, loadShape)
                                        .padding(14.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = A11yTokens.ButtonFill,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = "Chargement du mois…",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = A11yTokens.InkMuted
                                    )
                                }
                            }
                        }
                    }
                    state.timeline.isEmpty() -> {
                        item {
                            val emptyShape = MaterialTheme.shapes.medium
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                                    .border(1.dp, A11yTokens.PaperBorder, emptyShape)
                                    .background(A11yTokens.PaperCard, emptyShape)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Rien encore ce mois",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = A11yTokens.Ink
                                )
                                Text(
                                    text = "Ajoutez le salaire et les factures avec « + Ajouter » en bas.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = A11yTokens.InkMuted
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
                                                            onClickLabel = "Effacer la recherche",
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
                                        val searchTipShape = MaterialTheme.shapes.medium
                                        Text(
                                            text = "Le reste en haut est celui de tout le mois.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = A11yTokens.InkMuted,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, A11yTokens.PaperBorder, searchTipShape)
                                                .background(A11yTokens.PaperCard, searchTipShape)
                                                .padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                        if (searchQuery.isNotBlank() && filteredTimeline.isEmpty()) {
                            item(key = "search-empty") {
                                val searchEmptyShape = MaterialTheme.shapes.medium
                                val emptySearch =
                                    "Rien pour « ${searchQuery.trim()} ». " +
                                        "Les totaux du haut restent ceux du mois."
                                Text(
                                    text = emptySearch,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = A11yTokens.InkMuted,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .border(1.dp, A11yTokens.PaperBorder, searchEmptyShape)
                                        .background(A11yTokens.PaperCard, searchEmptyShape)
                                        .padding(14.dp)
                                        .semantics {
                                            liveRegion = LiveRegionMode.Polite
                                            contentDescription = emptySearch
                                        }
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
                        val diversShape = MaterialTheme.shapes.medium
                        val diversPad = if (fit.compactHomeChrome) 8.dp else 12.dp
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, A11yTokens.PaperBorder, diversShape)
                                .background(A11yTokens.PaperCard, diversShape)
                                .padding(diversPad),
                            verticalArrangement = Arrangement.spacedBy(
                                if (fit.compactHomeChrome) 2.dp else 4.dp
                            )
                        ) {
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
                                    ) { diversExpanded = !diversExpanded },
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
                                    "Noté ${MoneyFormat.formatCents(state.diversSpentCents)}"
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
                                            keyboardType = KeyboardType.Decimal,
                                            imeAction = ImeAction.Done
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = A11yTokens.minTouchHeight)
                                            .then(diversBring),
                                        textStyle = MaterialTheme.typography.bodyLarge
                                    )
                                    diversError?.let { err ->
                                        Spacer(modifier = Modifier.height(6.dp))
                                        val errShape = MaterialTheme.shapes.medium
                                        Text(
                                            text = err,
                                            color = A11yTokens.AccentDanger,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    A11yTokens.AccentDanger.copy(alpha = 0.4f),
                                                    errShape
                                                )
                                                .background(A11yTokens.PaperWarm, errShape)
                                                .padding(12.dp)
                                                .semantics {
                                                    liveRegion = LiveRegionMode.Assertive
                                                    contentDescription = err
                                                }
                                        )
                                    }
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
                                vertical = if (fit.compactHomeChrome) 6.dp else 8.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SecondaryA11yButton(
                                text = if (remindersBlocked) "Menu · Alertes" else "Menu",
                                onClick = {
                                    if (remindersBlocked) onOpenHealth() else menuOpen = true
                                },
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
            val verify = data.visuals.message == BelgianStructuredCommunication.VERIFY_ON_MAIL
            Snackbar(
                snackbarData = data,
                shape = MaterialTheme.shapes.medium,
                containerColor = if (verify) A11yTokens.PaperWarm else A11yTokens.Ink,
                contentColor = if (verify) A11yTokens.Ink else A11yTokens.ButtonText,
                actionColor = if (verify) A11yTokens.Ink else A11yTokens.ButtonText
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
private fun DayBadge(label: String, compact: Boolean = false) {
    val shape = MaterialTheme.shapes.medium
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = A11yTokens.InkMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .widthIn(min = if (compact) 48.dp else 56.dp)
            .background(A11yTokens.PaperWarm, shape)
            .border(1.dp, A11yTokens.PaperBorder, shape)
            .padding(
                horizontal = if (compact) 4.dp else 6.dp,
                vertical = if (compact) 2.dp else 4.dp
            )
            .semantics { contentDescription = "Échéance $label" }
    )
}

@Composable
private fun StatusChip(
    text: String,
    container: Color,
    content: Color,
    outlined: Boolean = false,
    compact: Boolean = false
) {
    val shape = MaterialTheme.shapes.medium
    val hPad = if (compact) 6.dp else 8.dp
    val vPad = if (compact) 2.dp else 4.dp
    Text(
        text = text,
        color = content,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .semantics { contentDescription = "Statut : $text" }
            .border(
                1.dp,
                if (outlined) A11yTokens.FieldLine else A11yTokens.PaperBorder,
                shape
            )
            .background(container, shape)
            .padding(horizontal = hPad, vertical = vPad)
    )
}

@Composable
private fun IncomeRow(item: TimelineItem.Income, onClick: () -> Unit) {
    val fit = rememberDisplayFit()
    val compact = fit.compactHomeChrome
    val rowPad = if (compact) 6.dp else 12.dp
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
                .padding(rowPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp)
        ) {
            DayBadge(item.dayLabel, compact = compact)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp)
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = if (compact) 1 else 2,
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
    val fit = rememberDisplayFit()
    val compact = fit.compactHomeChrome
    val rowPad = if (compact) 6.dp else 12.dp
    val shape = MaterialTheme.shapes.medium
    val bg = when {
        item.isDone -> A11yTokens.AccentOkSoft
        // Lavis discret : le contour 2 dp porte déjà l’alerte (pas un fond « criard »).
        item.isOverdue -> A11yTokens.AccentDanger.copy(alpha = 0.10f)
        else -> A11yTokens.PaperCard
    }
    val borderMod = when {
        highlighted -> Modifier.border(3.dp, A11yTokens.ButtonFill, shape)
        item.isOverdue && !item.isDone ->
            Modifier.border(2.dp, A11yTokens.AccentDanger, shape)
        else -> Modifier.border(1.dp, A11yTokens.PaperBorder, shape)
    }
    val lineGap = if (compact) 2.dp else 4.dp
    Surface(
        color = bg,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .then(borderMod)
    ) {
        Row(
            modifier = Modifier.padding(rowPad),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp)
        ) {
            DayBadge(item.dayLabel, compact = compact)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(lineGap)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClickLabel = "Modifier", onClick = onClick),
                    verticalArrangement = Arrangement.spacedBy(lineGap)
                ) {
                    // Compact + accord : pastille sur la 1ʳᵉ ligne pour ne pas
                    // pousser montant / FAIT (hauteur ~800 dp, plan sortie §3).
                    if (compact && item.isAgreement) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            StatusChip(
                                "Accord",
                                A11yTokens.PaperWarm,
                                A11yTokens.Ink,
                                outlined = true,
                                compact = true
                            )
                        }
                    } else {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = if (compact) 1 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Accord : « pour quoi » juste sous le nom (plan §B / audit),
                    // puis le montant — plus lisible que montant puis cause.
                    if (item.causeLabel.isNotBlank()) {
                        Text(
                            text = item.causeLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = item.amountLabel,
                        color = if (item.isDone) A11yTokens.InkMuted else A11yTokens.Ink,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                // Hors zone « Modifier » : Copier / photo ne doivent pas ouvrir l’édition.
                AnimatedVisibility(
                    visible = item.paymentNote.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.paymentNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted,
                            maxLines = if (compact) 1 else 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryA11yButton(
                            text = "Copier",
                            onClick = onCopyNote,
                            fillMaxWidth = false,
                            modifier = Modifier
                                .widthIn(min = if (compact) 88.dp else 96.dp)
                                .semantics {
                                    contentDescription = "Copier la communication"
                                }
                        )
                    }
                }
                ExpenseStatusLine(
                    item = item,
                    onOpenReceipt = onOpenReceipt,
                    hideAgreementChip = compact && item.isAgreement
                )
            }
            SecondaryA11yButton(
                text = if (item.isDone) "Remettre" else "FAIT ✓",
                onClick = onToggle,
                fillMaxWidth = false,
                modifier = Modifier
                    .widthIn(min = if (compact) 88.dp else 96.dp)
                    .semantics {
                        contentDescription = if (item.isDone) {
                            "Remettre à payer"
                        } else {
                            "Marquer FAIT"
                        }
                    }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpenseStatusLine(
    item: TimelineItem.Expense,
    onOpenReceipt: () -> Unit,
    hideAgreementChip: Boolean = false
) {
    val fit = rememberDisplayFit()
    val compact = fit.compactHomeChrome
    val showAuto = item.isAutoDebit
    val showAgreement = item.isAgreement && !hideAgreementChip
    val showDone = item.isDone
    val showLate = item.isOverdue && !item.isDone
    val showReceipt = item.hasReceipt
    if (!showAuto && !showAgreement && !showDone && !showLate && !showReceipt) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
    ) {
        if (showAgreement) {
            // Contour FieldLine : pastille discrète mais lisible sur papier court / texte large.
            StatusChip(
                "Accord",
                A11yTokens.PaperWarm,
                A11yTokens.Ink,
                outlined = true,
                compact = compact
            )
        }
        if (showAuto) {
            StatusChip(
                "Prélèvement",
                A11yTokens.AccentAuto,
                A11yTokens.Ink,
                compact = compact
            )
        }
        if (showDone) {
            StatusChip(
                item.doneLabel ?: "FAIT ✓",
                A11yTokens.AccentOk,
                A11yTokens.ButtonText,
                compact = compact
            )
        }
        if (showLate) {
            StatusChip(
                "En retard",
                A11yTokens.AccentDanger,
                A11yTokens.ButtonText,
                compact = compact
            )
        }
        if (showReceipt) {
            // Bouton réel (pas pastille Text) : le tap ouvre bien la photo.
            SecondaryA11yButton(
                text = if (compact) "Photo" else "Voir la photo",
                onClick = onOpenReceipt,
                fillMaxWidth = false,
                modifier = Modifier
                    .widthIn(min = if (compact) 80.dp else 96.dp)
                    .semantics { contentDescription = "Voir la photo de la facture" }
            )
        }
    }
}
