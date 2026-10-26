package be.monmoisclair.app.ui.home

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.graphicsLayer
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
import be.monmoisclair.app.ui.components.BrandMark
import be.monmoisclair.app.ui.components.CarnetPageTitle
import be.monmoisclair.app.ui.components.PrimaryA11yButton
import be.monmoisclair.app.ui.components.QuietLinkA11y
import be.monmoisclair.app.ui.components.SecondaryA11yButton
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.StampShape
import be.monmoisclair.app.ui.theme.carnetOutlinedTextFieldColors
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
    onOpenUpcoming: () -> Unit = {},
    onOpenSavings: () -> Unit = {},
    onOpenDeadlines: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val diversError by viewModel.diversError.collectAsStateWithLifecycle()
    val diversBusy by viewModel.diversBusy.collectAsStateWithLifecycle()
    val savingsError by viewModel.savingsError.collectAsStateWithLifecycle()
    val savingsBusy by viewModel.savingsBusy.collectAsStateWithLifecycle()
    val remindersBlocked by viewModel.remindersBlocked.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val listAlpha = remember { Animatable(1f) }
    var skipMonthFade by remember { mutableStateOf(true) }
    LaunchedEffect(state.selectedYearMonth) {
        if (skipMonthFade) {
            skipMonthFade = false
            return@LaunchedEffect
        }
        listAlpha.snapTo(0.72f)
        listAlpha.animateTo(1f, tween(160))
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fit = rememberDisplayFit()
    val keyboardOpen = imeOpen()
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var diversInput by remember { mutableStateOf("") }
    var savingsInput by remember { mutableStateOf("") }
    /** Unique UI Courses : sheet depuis le sticky (pas de formulaire en bas de liste). */
    var coursesSheetOpen by remember { mutableStateOf(false) }
    var savingsSheetOpen by remember { mutableStateOf(false) }
    /** Sticky : Reste + Courses + épargne ; le reste derrière « Plus ». */
    var stickyDetailsOpen by remember { mutableStateOf(false) }
    LaunchedEffect(state.isFuturePreview) {
        if (state.isFuturePreview) {
            coursesSheetOpen = false
            savingsSheetOpen = false
            stickyDetailsOpen = false
        }
    }
    LaunchedEffect(state.savingsMonthlyCents, savingsSheetOpen) {
        if (savingsSheetOpen) {
            savingsInput = if (state.savingsMonthlyCents > 0L) {
                MoneyFormat.centsToEditable(state.savingsMonthlyCents)
            } else {
                ""
            }
        }
    }
    var menuOpen by remember { mutableStateOf(false) }
    var monthPickerOpen by remember { mutableStateOf(false) }
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
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.shareIntents.collect { intent ->
            runCatching { context.startActivity(intent) }
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
        if (state.isFuturePreview) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Aperçu seulement — on ne coche pas FAIT sur un mois à venir."
                )
            }
            return
        }
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
                val agreementRemettre = !markingDone &&
                    state.timeline.filterIsInstance<TimelineItem.Expense>()
                        .any { it.id == expenseId && it.isAgreement }
                val result = snackbarHostState.showSnackbar(
                    message = when {
                        markingDone -> "Noté FAIT"
                        agreementRemettre ->
                            "Dossier rouvert — les mois suivants peuvent revenir"
                        else -> "Remis à payer"
                    },
                    actionLabel = "Annuler",
                    withDismissAction = true,
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
                    modifier = Modifier.padding(if (fit.compactHomeChrome) 12.dp else 16.dp),
                    verticalArrangement = Arrangement.spacedBy(
                        if (fit.compactHomeChrome) 8.dp else 12.dp
                    )
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
                                CircularProgressIndicator(color = A11yTokens.Link)
                            }
                        }
                        receiptBitmap != null -> {
                            Image(
                                bitmap = receiptBitmap!!.asImageBitmap(),
                                contentDescription = "Photo de la facture",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(
                                        min = if (fit.compactHomeChrome) 220.dp else 280.dp,
                                        max = if (fit.compactHomeChrome) 420.dp else 520.dp
                                    )
                                    .background(A11yTokens.PaperWarm, MaterialTheme.shapes.medium)
                            )
                        }
                        else -> {
                            val photoErrShape = MaterialTheme.shapes.medium
                            Text(
                                text = "Impossible d’afficher cette photo ici. " +
                                    "Essayez « Ouvrir à part », ou rouvrez la ligne pour en reprendre une.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = A11yTokens.DangerInk,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        A11yTokens.DangerInk.copy(alpha = 0.4f),
                                        photoErrShape
                                    )
                                    .background(A11yTokens.PaperWarm, photoErrShape)
                                    .padding(12.dp)
                                    .semantics {
                                        liveRegion = LiveRegionMode.Assertive
                                        contentDescription =
                                            "Impossible d’afficher cette photo. " +
                                                "Essayez ouvrir à part, ou reprenez-en une."
                                    }
                            )
                            SecondaryA11yButton(
                                text = "Ouvrir à part",
                                onClick = {
                                    val path = receiptPathToShow
                                    if (path.isNullOrBlank()) return@SecondaryA11yButton
                                    scope.launch {
                                        val intent = viewModel.viewReceiptIntent(path)
                                        if (intent == null) {
                                            snackbarHostState.showSnackbar(
                                                "Fichier introuvable — reprenez une photo."
                                            )
                                        } else {
                                            val ok = runCatching {
                                                context.startActivity(
                                                    Intent.createChooser(intent, "Voir la photo")
                                                )
                                            }.isSuccess
                                            if (!ok) {
                                                snackbarHostState.showSnackbar(
                                                    "Aucune app pour ouvrir cette photo."
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    SecondaryA11yButton(
                        text = "Fermer",
                        onClick = { receiptPathToShow = null }
                    )
                }
            }
        }
    }

    if (state.pendingPastToggleId != null) {
        // H14 : même papier que le dialog photo (PaperCream).
        AlertDialog(
            onDismissRequest = viewModel::dismissPastToggle,
            shape = MaterialTheme.shapes.medium,
            containerColor = A11yTokens.PaperCream,
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
                                    val agreementRemettre = !pastMarkingDone &&
                                        state.timeline
                                            .filterIsInstance<TimelineItem.Expense>()
                                            .any { it.id == id && it.isAgreement }
                                    val result = snackbarHostState.showSnackbar(
                                        message = when {
                                            pastMarkingDone -> "Noté FAIT"
                                            agreementRemettre ->
                                                "Dossier rouvert — les mois suivants peuvent revenir"
                                            else -> "Remis à payer"
                                        },
                                        actionLabel = "Annuler",
                                        withDismissAction = true,
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
            isPastMonth = state.isPastMonth,
            isFuturePreview = state.isFuturePreview,
            onDismiss = { menuOpen = false },
            onOpenMonthPicker = { monthPickerOpen = true },
            onGoToCurrentMonth = viewModel::goToCurrentMonth,
            onOpenSettings = onOpenSettings,
            dossierCount = state.dossierCount,
            hasDossiers = state.hasDossiers,
            pendingLetterCount = state.pendingLetterCount,
            onOpenDossiers = onOpenDossiers,
            onOpenUpcoming = onOpenUpcoming,
            remindersBlocked = remindersBlocked,
            onOpenHealth = onOpenHealth,
            onOpenSupport = viewModel::writeSupportMail
        )
    }
    if (monthPickerOpen) {
        HomeMonthPickerSheet(
            selectedYearMonth = state.selectedYearMonth,
            onDismiss = { monthPickerOpen = false },
            onSelectMonth = viewModel::goToMonth,
            onGoToCurrentMonth = viewModel::goToCurrentMonth
        )
    }
    if (coursesSheetOpen && !state.isFuturePreview) {
        HomeCoursesSheet(
            diversSpentCents = state.diversSpentCents,
            afterDiversCents = state.afterDiversCents,
            input = diversInput,
            onInputChange = {
                diversInput = it
                viewModel.clearDiversError()
            },
            error = diversError,
            busy = diversBusy,
            onAdd = { viewModel.addDivers(diversInput) },
            onRemove = { viewModel.removeDivers(diversInput) },
            onDismiss = { coursesSheetOpen = false }
        )
    }
    if (savingsSheetOpen && !state.isFuturePreview) {
        HomeSavingsSheet(
            monthlyCents = state.savingsMonthlyCents,
            input = savingsInput,
            onInputChange = {
                savingsInput = it
                viewModel.clearSavingsError()
            },
            error = savingsError,
            busy = savingsBusy,
            onSave = { viewModel.saveSavingsFromHome(savingsInput) },
            onOpenDetails = onOpenSavings,
            onDismiss = { savingsSheetOpen = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(A11yTokens.PaperCream)
                .imePadding()
        ) {
            // Sticky = même feuille crème que la liste (pas 2ᵉ bande PaperWarm) ;
            // socle Menu/+ reste warm. Pas de logo — Reste = hero.
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(A11yTokens.PaperCream)
                        .padding(horizontal = fit.stickyPaddingHorizontal)
                        .padding(vertical = fit.stickyPaddingVertical),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                // ‹ mois › — calendrier via le libellé (plus de 4ᵉ cible). Pas de logo.
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
                        // Quiet sticky : titleMedium muted — le Reste serif commande.
                        style = MaterialTheme.typography.titleMedium,
                        color = A11yTokens.InkMuted,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = A11yTokens.minTouchHeight)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Choisir un mois"
                            ) { monthPickerOpen = true }
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                    )
                    MonthStep(
                        iconRes = R.drawable.ic_chevron_right,
                        description = "Mois suivant",
                        enabled = state.canGoNext,
                        onClick = viewModel::nextMonth
                    )
                }
                val remainingTitle = when {
                    state.isFuturePreview && state.summary.remainingCents < 0L ->
                        "Aperçu — déficit après factures"
                    state.isFuturePreview -> "Aperçu — reste après factures"
                    state.summary.remainingCents < 0L -> "Déficit après factures"
                    else -> "Reste après factures"
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
                        state.summary.remainingCents < 0L -> A11yTokens.DangerInk
                        else -> A11yTokens.Ink
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "$remainingTitle : $remainingAmount"
                    }
                )
                // H01 : 1 chiffre + 1 ligne statut ; Courses = entrée sticky (sheet unique).
                stickyStillToPayLabel(
                    stillToPayCents = state.summary.stillToPayCents,
                    hasExpenses = state.timeline.any { it is TimelineItem.Expense },
                    futurePreview = state.isFuturePreview
                )?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = when {
                            state.summary.stillToPayCents == 0L -> A11yTokens.AccentOkInk
                            else -> A11yTokens.InkMuted
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { contentDescription = label }
                    )
                }
                stickyCoursesLabel(
                    diversSpentCents = state.diversSpentCents,
                    afterDiversCents = state.afterDiversCents,
                    futurePreview = state.isFuturePreview
                )?.let { coursesLabel ->
                    StickyLinkRow(
                        label = coursesLabel,
                        labelColor = when {
                            state.diversSpentCents > 0L && state.afterDiversCents < 0L ->
                                A11yTokens.DangerInk
                            else -> A11yTokens.InkMuted
                        },
                        compact = fit.compactHomeChrome,
                        onClickLabel = "Modifier les courses",
                        contentDescription = if (state.diversSpentCents > 0L) {
                            "$coursesLabel. Modifier les courses"
                        } else {
                            "Courses. Noter un montant"
                        },
                        onClick = { coursesSheetOpen = true }
                    )
                }
                // S26 / hauteur courte : épargne derrière Plus (toujours accessible).
                val savingsLabelSticky = stickySavingsLabel(
                    savingsMonthlyCents = state.savingsMonthlyCents,
                    futurePreview = state.isFuturePreview
                )
                val savingsBehindPlus =
                    fit.compactHomeChrome && savingsLabelSticky != null
                if (!savingsBehindPlus) {
                    savingsLabelSticky?.let { savingsLabel ->
                        StickyLinkRow(
                            label = savingsLabel,
                            labelColor = A11yTokens.InkMuted,
                            compact = fit.compactHomeChrome,
                            onClickLabel = "Modifier mettre de côté",
                            contentDescription = if (state.savingsMonthlyCents > 0L) {
                                "$savingsLabel. Modifier"
                            } else {
                                "Mettre de côté. Choisir un montant"
                            },
                            onClick = { savingsSheetOpen = true }
                        )
                    }
                }
                // Mode aperçu = contexte, toujours visible (pas derrière « Plus »).
                if (state.isFuturePreview) {
                    val previewLabel =
                        "Aperçu — montants d’après la dernière saisie ; pas de FAIT ni d’ajout."
                    Text(
                        text = previewLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = A11yTokens.AccentWarn,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = previewLabel
                        }
                    )
                }
                val afterFaitLabel = stickyWithoutUnpaidLabel(
                    remainingCents = state.summary.remainingCents,
                    stillToPayCents = state.summary.stillToPayCents,
                    futurePreview = state.isFuturePreview
                )
                val needIncomeTip = !state.isFuturePreview &&
                    state.summary.remainingCents < 0L &&
                    state.timeline.none { it is TimelineItem.Income }
                val hasStickyExtras = afterFaitLabel != null ||
                    needIncomeTip ||
                    state.nextMonthWarning != null ||
                    state.pendingLetterCount > 0 ||
                    savingsBehindPlus
                if (hasStickyExtras) {
                    val detailsLabel =
                        if (stickyDetailsOpen) "Masquer" else "Autres infos"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = A11yTokens.minTouchHeight)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = if (stickyDetailsOpen) {
                                    "Masquer épargne et détails"
                                } else {
                                    "Voir épargne et détails"
                                }
                            ) { stickyDetailsOpen = !stickyDetailsOpen }
                            .padding(vertical = 4.dp)
                            .semantics { contentDescription = detailsLabel },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = detailsLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = A11yTokens.InkMuted,
                            maxLines = 1
                        )
                        Icon(
                            painter = painterResource(
                                if (stickyDetailsOpen) R.drawable.ic_expand_less
                                else R.drawable.ic_expand_more
                            ),
                            contentDescription = null,
                            tint = A11yTokens.InkMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                AnimatedVisibility(visible = stickyDetailsOpen && hasStickyExtras) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(fit.sectionGap)
                    ) {
                        if (savingsBehindPlus) {
                            savingsLabelSticky?.let { savingsLabel ->
                                StickyLinkRow(
                                    label = savingsLabel,
                                    labelColor = A11yTokens.InkMuted,
                                    compact = true,
                                    onClickLabel = "Modifier mettre de côté",
                                    contentDescription = if (state.savingsMonthlyCents > 0L) {
                                        "$savingsLabel. Modifier"
                                    } else {
                                        "Mettre de côté. Choisir un montant"
                                    },
                                    onClick = { savingsSheetOpen = true }
                                )
                            }
                        }
                        afterFaitLabel?.let { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = A11yTokens.AccentWarn,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics { contentDescription = label }
                            )
                        }
                        if (needIncomeTip) {
                            Text(
                                text = "Ajoutez un revenu (salaire).",
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.AccentWarn,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics {
                                    liveRegion = LiveRegionMode.Polite
                                    contentDescription = "Ajoutez un revenu (salaire)."
                                }
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
                                    .fillMaxWidth()
                                    .heightIn(min = A11yTokens.minTouchHeight)
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = "Voir les échéances hors ce mois"
                                    ) { onOpenDeadlines() }
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
                            Text(
                                text = pendingLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = A11yTokens.InkMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = A11yTokens.minTouchHeight)
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = "Ouvrir Dossiers"
                                    ) { onOpenDossiers() }
                                    .semantics {
                                        liveRegion = LiveRegionMode.Polite
                                        contentDescription = pendingLabel
                                    }
                            )
                        }
                    }
                }
            }
            }
            // Un seul filet sous le sticky (plus de double séparateur dégradé + divider).
            HorizontalDivider(color = A11yTokens.PaperBorder)

            // FAIT : bandeau papier compact sous le sticky (pas barre pleine largeur).
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = fit.screenPadding)
                    .padding(top = 4.dp, bottom = 2.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = MaterialTheme.shapes.small,
                    containerColor = A11yTokens.PaperCard,
                    contentColor = A11yTokens.Ink,
                    actionColor = A11yTokens.Link,
                    modifier = Modifier
                        .wrapContentWidth()
                        .border(1.dp, A11yTokens.PaperBorder, MaterialTheme.shapes.small)
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer { alpha = listAlpha.value },
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
                                    .padding(vertical = if (fit.compactHomeChrome) 32.dp else 48.dp)
                                    .semantics {
                                        contentDescription = "Chargement du mois"
                                        liveRegion = LiveRegionMode.Polite
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(
                                    if (fit.compactHomeChrome) 12.dp else 16.dp
                                )
                            ) {
                                // LOAD01 : brand + texte, sans carte ni gros spinner.
                                BrandMark(height = 32.dp)
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
                            // H08 : empty sans carte tip ni BrandMark.
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = if (fit.compactHomeChrome) 16.dp else 24.dp),
                                verticalArrangement = Arrangement.spacedBy(
                                    if (fit.compactHomeChrome) 6.dp else 8.dp
                                )
                            ) {
                                CarnetPageTitle(
                                    if (state.isFuturePreview) {
                                        "Rien de prévu ce mois-là"
                                    } else {
                                        "Rien encore ce mois"
                                    },
                                    withTopSpacer = false
                                )
                                Text(
                                    text = if (state.isFuturePreview) {
                                        "Les factures et rentrées réapparaissent ici quand elles sont dues. " +
                                            "Revenez au mois en cours pour ajouter."
                                    } else {
                                        "Ajoutez le salaire et les factures avec « + Ajouter » en bas."
                                    },
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
                                        colors = carnetOutlinedTextFieldColors(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = A11yTokens.minTouchHeight),
                                        textStyle = MaterialTheme.typography.bodyLarge,
                                        trailingIcon = if (searchQuery.isNotEmpty()) {
                                            {
                                                Text(
                                                    text = "Effacer",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = A11yTokens.Link,
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
                                    // Message vide juste sous le champ (au-dessus du tip) :
                                    // sinon clavier Samsung masque le Lazy item suivant (C7).
                                    if (searchQuery.isNotBlank() && filteredTimeline.isEmpty()) {
                                        val emptySearch =
                                            "Rien pour « ${searchQuery.trim()} ». " +
                                                "Les totaux du haut restent ceux du mois."
                                        Text(
                                            text = emptySearch,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = A11yTokens.InkMuted,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                                .semantics {
                                                    liveRegion = LiveRegionMode.Polite
                                                }
                                        )
                                    } else if (searchQuery.isNotBlank()) {
                                        // H09 : tip une ligne sans carte.
                                        Text(
                                            text = "Le reste en haut est celui de tout le mois.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = A11yTokens.InkMuted,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
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
                                    readOnlyPreview = state.isFuturePreview,
                                    onClick = {
                                        if (state.isFuturePreview) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Aperçu seulement — ouvrez le mois en cours pour modifier."
                                                )
                                            }
                                        } else {
                                            onEdit(item.id, false)
                                        }
                                    }
                                )
                                is TimelineItem.Expense -> ExpenseRow(
                                    item = item,
                                    highlighted = item.id == state.highlightExpenseId,
                                    readOnlyPreview = state.isFuturePreview,
                                    onToggle = { onFaitToggle(item.id, markingDone = !item.isDone) },
                                    onClick = {
                                        if (state.isFuturePreview) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Aperçu seulement — ouvrez le mois en cours pour modifier."
                                                )
                                            }
                                            return@ExpenseRow
                                        }
                                        viewModel.clearHighlight()
                                        onEdit(item.id, true)
                                    },
                                    onCopyNote = {
                                        viewModel.copyPaymentNote(item.paymentNote)
                                    },
                                    onOpenReceipt = {
                                        val raw = item.receiptRelativePath.trim()
                                        val path = viewModel.primaryReceiptPath(raw)
                                        if (path.isEmpty()) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Aucune photo liée à cette facture."
                                                )
                                            }
                                        } else if (viewModel.isPdfReceipt(path)) {
                                            scope.launch {
                                                val intent = viewModel.viewReceiptIntent(path)
                                                if (intent == null) {
                                                    snackbarHostState.showSnackbar(
                                                        "Impossible d’ouvrir ce courrier."
                                                    )
                                                } else {
                                                    val ok = runCatching {
                                                        context.startActivity(
                                                            Intent.createChooser(
                                                                intent,
                                                                "Voir le courrier"
                                                            )
                                                        )
                                                    }.isSuccess
                                                    if (!ok) {
                                                        snackbarHostState.showSnackbar(
                                                            "Aucune app pour ouvrir ce PDF."
                                                        )
                                                    }
                                                }
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
            }

            if (!keyboardOpen) {
                HomeBottomBar(
                    isFuturePreview = state.isFuturePreview,
                    remindersBlocked = remindersBlocked,
                    onMenu = { menuOpen = true },
                    onAdd = { onAdd(state.selectedYearMonth) },
                    onGoCurrentMonth = viewModel::goToCurrentMonth
                )
            }
        }
    }
}
