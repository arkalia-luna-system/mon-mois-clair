package be.monmoisclair.app.ui.home

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.monmoisclair.app.MainActivity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import be.monmoisclair.app.ui.components.BrandMark
import be.monmoisclair.app.ui.format.MoneyFormat
import be.monmoisclair.app.domain.CarnetListLimits
import be.monmoisclair.app.domain.HomeTodayFocus
import be.monmoisclair.app.ui.components.CarnetTipCard
import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.ui.theme.HomeTokens
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
    onOpenHelp: () -> Unit,
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
    val whatsNew by viewModel.whatsNew.collectAsStateWithLifecycle()
    val backupNudgeVisible by viewModel.backupNudgeVisible.collectAsStateWithLifecycle()
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
    /** Sticky : Détails secondaires ; Courses|épargne à côté du Reste (7.08). */
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
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshReminderGate()
                // B10 : si le process a survécu au changement de mois calendaire.
                viewModel.refreshCurrentMonthIfRolled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.diversFieldCleared.collect { diversInput = "" }
    }

    LaunchedEffect(Unit) {
        viewModel.userMessages.collect { msg ->
            // Pendant snackbar FAIT : seulement ignorer « Copié » (pas les erreurs).
            if (faitUndoActive && msg.startsWith("Copié")) return@collect
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.shareIntents.collect { intent ->
            runCatching { context.startActivity(intent) }
                .onFailure { viewModel.reportShareIntentFailed() }
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

    val todayFocus = remember(state.timeline, state.selectedYearMonth, state.isFuturePreview) {
        if (state.isFuturePreview) {
            HomeTodayFocus.Result(emptyList(), emptyList())
        } else {
            val inputs = state.timeline.filterIsInstance<TimelineItem.Expense>().map {
                HomeTodayFocus.ExpenseInput(
                    id = it.id,
                    label = it.label,
                    dayOfMonth = it.sortDay,
                    status = if (it.isDone) ExpenseStatus.DONE else ExpenseStatus.TODO
                )
            }
            HomeTodayFocus.compute(inputs, state.selectedYearMonth)
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
                        item.causeLabel.contains(q, ignoreCase = true) ||
                        item.paymentNote.contains(q, ignoreCase = true)
            }
        }
    }
    /** Plafond d’affichage LazyColumn — la recherche s’applique sur la liste complète avant la coupe. */
    val listTimeline = remember(filteredTimeline) {
        CarnetListLimits.truncate(
            filteredTimeline,
            CarnetListLimits.MAX_HOME_TIMELINE_ITEMS
        )
    }
    val searchFieldVisible = !state.loading && state.timeline.size >= 4

    // P3-F02 : scroller dans la liste filtrée (+ offset du champ recherche).
    LaunchedEffect(state.highlightExpenseId, listTimeline, searchQuery, searchFieldVisible) {
        val id = state.highlightExpenseId ?: return@LaunchedEffect
        var index = listTimeline.visible.indexOfFirst {
            it is TimelineItem.Expense && it.id == id
        }
        if (index < 0 && searchQuery.isNotBlank()) {
            searchQuery = ""
            return@LaunchedEffect
        }
        if (index < 0) return@LaunchedEffect
        // Offset tête de liste : statut factures (si présent) + champ recherche.
        val hasStillToPay = stickyStillToPayLabel(
            stillToPayCents = state.summary.stillToPayCents,
            hasExpenses = state.timeline.any { it is TimelineItem.Expense },
            futurePreview = state.isFuturePreview
        ) != null
        if (todayFocus.hasAction) index += 1
        if (hasStillToPay) index += 1
        if (searchFieldVisible) index += 1
        listState.animateScrollToItem(index)
    }

    var pastMarkingDone by remember { mutableStateOf(true) }

    fun showBriefHint(message: String) {
        // Remplace le snackbar courant : pas de file d’attente / spam sur taps répétés.
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
        }
    }

    fun onFaitToggle(expenseId: String, markingDone: Boolean) {
        if (state.isFuturePreview) {
            showBriefHint("Pas de FAIT sur un mois à venir — ouvrez le mois en cours.")
            return
        }
        // Mois passé : dialogue d’abord — pas de snackbar anticipé.
        if (state.isPastMonth) {
            pastMarkingDone = markingDone
            viewModel.requestToggleDone(expenseId)
            return
        }
        // Snackbar optimiste : immédiat, mais seulement si le toggle est accepté
        // (pas de 2ᵉ bandeau contradictoire sur double tap).
        if (!viewModel.requestToggleDone(expenseId)) return
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            faitUndoActive = true
            try {
                val agreementRemettre = !markingDone &&
                    state.timeline.filterIsInstance<TimelineItem.Expense>()
                        .any { it.id == expenseId && it.isAgreement }
                val result = snackbarHostState.showSnackbar(
                    message = when {
                        markingDone -> "Noté FAIT — touchez Annuler si erreur"
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


    whatsNew?.let { payload ->
        HomeWhatsNewDialog(
            bullets = payload.bullets,
            versionLabel = payload.versionLabel,
            fit = fit,
            onDismiss = viewModel::dismissWhatsNew,
        )
    }
    if (backupNudgeVisible) {
        HomeBackupNudgeDialog(
            onLater = viewModel::dismissBackupNudge,
            onOpenBackup = {
                viewModel.consumeBackupNudgeOpenSettings()
                onOpenSettings()
            }
        )
    }

    HomeReceiptDialog(
        title = receiptTitle,
        path = receiptPathToShow,
        bitmap = receiptBitmap,
        loading = receiptLoading,
        fit = fit,
        snackbarHostState = snackbarHostState,
        scope = scope,
        viewModel = viewModel,
        onDismiss = { receiptPathToShow = null }
    )
    HomePastToggleDialog(
        monthLabel = state.monthLabel,
        pastMarkingDone = pastMarkingDone,
        pendingId = state.pendingPastToggleId,
        timeline = state.timeline,
        haptics = haptics,
        snackbarHostState = snackbarHostState,
        scope = scope,
        viewModel = viewModel,
        onFaitUndoActive = { faitUndoActive = it },
        onDismiss = viewModel::dismissPastToggle
    )

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
            onOpenHelp = onOpenHelp,
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
                .background(HomeTokens.Canvas)
                .imePadding()
        ) {
            HomeStickyHeader(
                state = state,
                fit = fit,
                stickyDetailsOpen = stickyDetailsOpen,
                onStickyDetailsOpenChange = { stickyDetailsOpen = it },
                onOpenMonthPicker = { monthPickerOpen = true },
                onPrevMonth = viewModel::prevMonth,
                onNextMonth = viewModel::nextMonth,
                onOpenDeadlines = onOpenDeadlines,
                onOpenDossiers = onOpenDossiers,
                onGoToCurrentMonth = viewModel::goToCurrentMonth,
                // 7.09 : chips chariot | tirelire à côté du Reste (1 tap, pas Menu).
                coursesLabel = stickyCoursesChipLabel(
                    diversSpentCents = state.diversSpentCents,
                    futurePreview = state.isFuturePreview
                ),
                coursesDescription = stickyCoursesLabel(
                    diversSpentCents = state.diversSpentCents,
                    afterDiversCents = state.afterDiversCents,
                    futurePreview = state.isFuturePreview
                ),
                onOpenCourses = { coursesSheetOpen = true },
                savingsLabel = stickySavingsChipLabel(
                    savingsMonthlyCents = state.savingsMonthlyCents,
                    futurePreview = state.isFuturePreview
                ),
                savingsDescription = stickySavingsLabel(
                    savingsMonthlyCents = state.savingsMonthlyCents,
                    futurePreview = state.isFuturePreview
                ),
                onOpenSavings = { savingsSheetOpen = true }
            )
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
                    shape = HomeTokens.cardRadius,
                    containerColor = HomeTokens.Card,
                    contentColor = HomeTokens.Ink,
                    actionColor = HomeTokens.Link,
                    modifier = Modifier
                        .wrapContentWidth()
                        .border(1.dp, HomeTokens.CardBorder, HomeTokens.cardRadius)
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
                    top = if (fit.compactHomeChrome) 12.dp else 16.dp,
                    bottom = if (fit.compactHomeChrome) 8.dp else 12.dp
                ),
                // 7.09 : air cartes Accueil (HomeTokens).
                verticalArrangement = Arrangement.spacedBy(HomeTokens.cardGap)
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
                                    color = HomeTokens.InkMuted
                                )
                            }
                        }
                    }
                    state.timeline.isEmpty() -> {
                        item {
                            // 7.13 : empty premium — carte soft + fade calme.
                            val emptyShape = HomeTokens.heroRadius
                            AnimatedVisibility(visible = true, enter = fadeIn(tween(280))) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = if (fit.compactHomeChrome) {
                                                16.dp
                                            } else {
                                                HomeTokens.emptyPadVertical
                                            }
                                        )
                                        .shadow(HomeTokens.cardElevation, emptyShape, clip = false)
                                        .clip(emptyShape)
                                        .background(HomeTokens.HeroSurface)
                                        .padding(
                                            horizontal = 20.dp,
                                            vertical = if (fit.compactHomeChrome) 20.dp else 28.dp
                                        ),
                                    verticalArrangement = Arrangement.spacedBy(
                                        if (fit.compactHomeChrome) 10.dp else 12.dp
                                    )
                                ) {
                                    Text(
                                        text = HomeEmptyMonthCopy.title(
                                            isPastMonth = state.isPastMonth,
                                            isFuturePreview = state.isFuturePreview
                                        ),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = HomeTokens.Ink
                                    )
                                    Text(
                                        text = HomeEmptyMonthCopy.body(
                                            isPastMonth = state.isPastMonth,
                                            isFuturePreview = state.isFuturePreview
                                        ),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = HomeTokens.InkMuted
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        if (todayFocus.hasAction) {
                            item(key = "today-focus") {
                                HomeTodayFocusCard(
                                    focus = todayFocus,
                                    onShowFirst = {
                                        todayFocus.firstExpenseId?.let(viewModel::setHighlightExpenseId)
                                    },
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                            }
                        }
                        // 7.07 : statut factures hors sticky — tête de liste (scroll).
                        stickyStillToPayLabel(
                            stillToPayCents = state.summary.stillToPayCents,
                            hasExpenses = state.timeline.any { it is TimelineItem.Expense },
                            futurePreview = state.isFuturePreview
                        )?.let { label ->
                            item(key = "still-to-pay") {
                                // 7.11 : bandeau statut mist (micro-rythme sous hero).
                                val paidOk = state.summary.stillToPayCents == 0L
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (paidOk) HomeTokens.Ok else HomeTokens.Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(HomeTokens.chipRadius)
                                        .background(
                                            if (paidOk) HomeTokens.OkSoft else HomeTokens.Chip
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .semantics { contentDescription = label }
                                )
                            }
                        }
                        CarnetListLimits.hiddenListHint(listTimeline.hiddenCount)?.let { hint ->
                            item(key = "timeline-truncate") {
                                CarnetTipCard(text = hint)
                            }
                        }
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
                                            .heightIn(min = HomeTokens.minTouch),
                                        textStyle = MaterialTheme.typography.bodyLarge,
                                        trailingIcon = if (searchQuery.isNotEmpty()) {
                                            {
                                                Text(
                                                    text = "Effacer",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = HomeTokens.Link,
                                                    modifier = Modifier
                                                        .heightIn(min = HomeTokens.minTouch)
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
                                            color = HomeTokens.InkMuted,
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
                                            color = HomeTokens.InkMuted,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                        items(listTimeline.visible, key = { item ->
                            when (item) {
                                is TimelineItem.Income -> "i-${item.id}"
                                is TimelineItem.Expense -> "e-${item.id}"
                            }
                        }) { item ->
                            when (item) {
                                is TimelineItem.Income -> IncomeRow(
                                    item = item,
                                    // readOnlyPreview n’affecte que le libellé a11y ici.
                                    readOnlyPreview = false,
                                    onClick = { onEdit(item.id, false) }
                                )
                                is TimelineItem.Expense -> ExpenseRow(
                                    item = item,
                                    highlighted = item.id == state.highlightExpenseId,
                                    // Cache FAIT sur mois futur ; édition ligne = OK.
                                    readOnlyPreview = state.isFuturePreview,
                                    onToggle = { onFaitToggle(item.id, markingDone = !item.isDone) },
                                    onClick = {
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
                                                        be.monmoisclair.app.ui.ExternalIntentMessages.RECEIPT_UNAVAILABLE
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
                                                            be.monmoisclair.app.ui.ExternalIntentMessages.NO_PDF_VIEWER
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
                    showGoCurrentCta = HomeEmptyMonthCopy.showGoCurrentCta(
                        isPastMonth = state.isPastMonth,
                        isFuturePreview = state.isFuturePreview
                    ),
                    remindersBlocked = remindersBlocked,
                    onMenu = { menuOpen = true },
                    onAdd = { onAdd(state.selectedYearMonth) },
                    onGoCurrentMonth = viewModel::goToCurrentMonth
                )
            }
        }
    }
}
