package be.monmoisclair.app.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.MonthLedgerDao
import be.monmoisclair.app.data.db.dao.PendingLetterDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.data.reminder.ReminderHealthChecker
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.ExpenseOverdue
import be.monmoisclair.app.domain.MonthCycleEngine
import be.monmoisclair.app.domain.MonthNavigation
import be.monmoisclair.app.domain.MonthSummary
import be.monmoisclair.app.domain.MonthSummaryCalculator
import be.monmoisclair.app.domain.ReceiptPathList
import be.monmoisclair.app.domain.SupportMailComposer
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.domain.usecase.AdjustDiversResult
import be.monmoisclair.app.domain.usecase.AdjustDiversUseCase
import be.monmoisclair.app.domain.usecase.ConfigureSavingsResult
import be.monmoisclair.app.domain.usecase.ConfigureSavingsUseCase
import be.monmoisclair.app.domain.usecase.ListDossiersUseCase
import be.monmoisclair.app.domain.usecase.ListNextMonthPeriodicsUseCase
import be.monmoisclair.app.domain.usecase.ToggleExpenseDoneResult
import be.monmoisclair.app.domain.usecase.ToggleExpenseDoneUseCase
import be.monmoisclair.app.ui.clipboard.SensitiveClipboard
import be.monmoisclair.app.ui.format.DayLabelFormat
import be.monmoisclair.app.ui.format.DoneDateFormat
import be.monmoisclair.app.ui.format.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val ledgerId: String = "",
    val selectedYearMonth: java.time.YearMonth = AppClock.currentYearMonth(),
    val monthLabel: String = "",
    val isPastMonth: Boolean = false,
    /** Aperçu M+1…M+3 : factures/revenus visibles, pas de FAIT / ajout. */
    val isFuturePreview: Boolean = false,
    val canGoPrev: Boolean = false,
    val canGoNext: Boolean = false,
    val summary: MonthSummary = MonthSummary(0, 0, 0, 0),
    val diversSpentCents: Long = 0L,
    val afterDiversCents: Long = 0L,
    val timeline: List<TimelineItem> = emptyList(),
    val highlightExpenseId: String? = null,
    /** Demande confirm FAIT sur mois passé (T4). */
    val pendingPastToggleId: String? = null,
    val nextMonthWarning: String? = null,
    /**
     * Lot D — dossiers **en cours** (non soldés) pour le libellé Menu.
     * Soldés exclus du chiffre (H01 smoke : « (4) » ≠ 2 cartes En cours).
     */
    val dossierCount: Int = 0,
    /** true s’il existe au moins un accord (ouvert ou soldé) → Menu visible. */
    val hasDossiers: Boolean = false,
    /** Lot D — bandeau accueil si > 0. */
    val pendingLetterCount: Int = 0,
    /** Lot E — forfait mensuel « Mettre de côté » (0 = inactif). */
    val savingsMonthlyCents: Long = 0L
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val monthCycleEngine: MonthCycleEngine,
    private val monthLedgerDao: MonthLedgerDao,
    private val instanceDao: InstanceDao,
    private val templateDao: TemplateDao,
    private val appMetaDao: AppMetaDao,
    private val pendingLetterDao: PendingLetterDao,
    private val toggleDone: ToggleExpenseDoneUseCase,
    private val adjustDivers: AdjustDiversUseCase,
    private val configureSavings: ConfigureSavingsUseCase,
    private val listNextMonthPeriodics: ListNextMonthPeriodicsUseCase,
    private val listDossiers: ListDossiersUseCase,
    private val reminderHealth: ReminderHealthChecker,
    private val receiptPhotos: ReceiptPhotoStore
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(AppClock.currentYearMonth())
    /** Calendrier vu au dernier resume — pour détecter un changement de mois (B10). */
    private var lastCalendarMonth: java.time.YearMonth = AppClock.currentYearMonth()
    private val highlightExpenseId = MutableStateFlow<String?>(null)
    private val pendingPastToggleId = MutableStateFlow<String?>(null)
    private val nextMonthWarning = MutableStateFlow<String?>(null)
    private val _diversError = MutableStateFlow<String?>(null)
    val diversError: StateFlow<String?> = _diversError.asStateFlow()
    private val _remindersBlocked = MutableStateFlow(false)
    val remindersBlocked: StateFlow<Boolean> = _remindersBlocked.asStateFlow()
    private val _diversBusy = MutableStateFlow(false)
    val diversBusy: StateFlow<Boolean> = _diversBusy.asStateFlow()
    private val _diversFieldCleared = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val diversFieldCleared: SharedFlow<Unit> = _diversFieldCleared.asSharedFlow()
    private val _savingsBusy = MutableStateFlow(false)
    val savingsBusy: StateFlow<Boolean> = _savingsBusy.asStateFlow()
    private val _savingsError = MutableStateFlow<String?>(null)
    val savingsError: StateFlow<String?> = _savingsError.asStateFlow()
    private val _userMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val userMessages: SharedFlow<String> = _userMessages.asSharedFlow()
    private val _shareIntents = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    val shareIntents: SharedFlow<Intent> = _shareIntents.asSharedFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        selectedMonth.flatMapLatest { ym ->
            val ledgerId = AppClock.ledgerId(ym)
            combine(
                instanceDao.observeIncomes(ledgerId),
                instanceDao.observeExpenses(ledgerId),
                templateDao.observeExpenseTemplates(),
                monthLedgerDao.observeAll(),
                combine(
                    highlightExpenseId,
                    pendingPastToggleId,
                    appMetaDao.observeValue(AppMetaKeys.SAVINGS_TEMPLATE_ID)
                ) { h, p, savingsId -> Triple(h, p, savingsId) }
            ) { incomes, expenses, templates, ledgers, highlightTriple ->
                val (highlight, pendingToggle, savingsMetaId) = highlightTriple
                val tplById = templates.associateBy { it.id }
                val summary = MonthSummaryCalculator.compute(incomes, expenses)
                val divers = ledgers.firstOrNull { it.id == ledgerId }?.diversSpentCents ?: 0L
                val timeline = buildList {
                    incomes.forEach { inc ->
                        add(
                            TimelineItem.Income(
                                id = inc.id,
                                dayLabel = DayLabelFormat.dayOfMonthLabel(inc.dayOfMonth),
                                label = inc.label,
                                amountLabel = MoneyFormat.formatSignedCents(inc.amountCents, isExpense = false),
                                sortDay = inc.dayOfMonth,
                                sortLabel = inc.label
                            )
                        )
                    }
                    expenses.forEach { exp ->
                        val tpl = exp.templateId?.let { tplById[it] }
                        val savingsLine = !savingsMetaId.isNullOrBlank() &&
                            exp.templateId == savingsMetaId
                        add(
                            TimelineItem.Expense(
                                id = exp.id,
                                dayLabel = DayLabelFormat.dayOfMonthLabel(exp.dayOfMonth),
                                label = exp.label,
                                amountLabel = MoneyFormat.formatSignedCents(exp.amountCents, isExpense = true),
                                isDone = exp.status == ExpenseStatus.DONE,
                                doneLabel = DoneDateFormat.label(exp.doneAtEpochMs),
                                isOverdue = ExpenseOverdue.isOverdue(
                                    ledgerId = exp.ledgerId,
                                    dayOfMonth = exp.dayOfMonth,
                                    status = exp.status,
                                    // Épargne = jamais « en retard », même si libellé abîmé.
                                    label = if (savingsLine) {
                                        ConfigureSavingsUseCase.LABEL
                                    } else {
                                        exp.label
                                    }
                                ),
                                isAutoDebit = exp.isAutoDebit,
                                paymentNote = exp.paymentNote,
                                hasReceipt = run {
                                    val primary = ReceiptPathList.primaryPhotoOrFirst(
                                        exp.receiptRelativePath,
                                        receiptPhotos::isPdf
                                    )
                                    primary.isNotBlank() && !receiptPhotos.isPdf(primary)
                                },
                                receiptRelativePath = exp.receiptRelativePath,
                                causeLabel = tpl?.causeLabel.orEmpty(),
                                isAgreement = DossierRemaining.isDossier(tpl?.openingRemainingCents),
                                intervalMonths = tpl?.intervalMonths ?: 1,
                                sortDay = exp.dayOfMonth,
                                sortLabel = exp.label
                            )
                        )
                    }
                }.sortedWith(compareBy({ it.sortDay }, { it.sortLabel }))

                val current = AppClock.currentYearMonth()
                val hasDossiers = templates.any {
                    it.active && DossierRemaining.isDossier(it.openingRemainingCents)
                }
                val savingsId = savingsMetaId?.trim().orEmpty()
                val savingsTpl = when {
                    savingsId.isNotEmpty() ->
                        templates.firstOrNull { it.active && it.id == savingsId }
                    else -> templates.firstOrNull {
                        it.active && it.label == ConfigureSavingsUseCase.LABEL
                    }
                }
                HomeUiState(
                    loading = false,
                    ledgerId = ledgerId,
                    selectedYearMonth = ym,
                    monthLabel = DayLabelFormat.monthTitle(ym),
                    isPastMonth = ym.isBefore(current),
                    isFuturePreview = MonthNavigation.isFuturePreview(ym, current),
                    canGoPrev = MonthNavigation.canGoPrev(ym, current),
                    canGoNext = MonthNavigation.canGoNext(ym, current),
                    summary = summary,
                    diversSpentCents = divers,
                    afterDiversCents = summary.remainingCents - divers,
                    timeline = timeline,
                    highlightExpenseId = highlight,
                    pendingPastToggleId = pendingToggle,
                    hasDossiers = hasDossiers,
                    savingsMonthlyCents = savingsTpl?.amountCents ?: 0L
                )
            }.mapLatest { base ->
                // Compteur Menu = En cours seulement (soldés exclus du chiffre).
                val snap = listDossiers()
                val openCount = snap.items.count { !it.isSettled }
                base.copy(
                    dossierCount = openCount,
                    hasDossiers = snap.items.isNotEmpty() || base.hasDossiers
                )
            }
        },
        nextMonthWarning,
        pendingLetterDao.observeCount()
    ) { base, warning, pendingCount ->
        base.copy(nextMonthWarning = warning, pendingLetterCount = pendingCount)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HomeUiState()
        )

    init {
        viewModelScope.launch {
            monthCycleEngine.ensureCurrentMonthExists()
            selectedMonth.value = AppClock.currentYearMonth()
        }
        viewModelScope.launch {
            selectedMonth.collect { refreshNextMonthWarning(it) }
        }
        refreshReminderGate()
    }

    /** Pastille Menu : notifications ou alarmes exactes refusées. */
    fun refreshReminderGate() {
        viewModelScope.launch {
            _remindersBlocked.value = reminderHealth.check().remindersBlocked
        }
    }

    fun refreshNextMonthWarning(ym: java.time.YearMonth = selectedMonth.value) {
        viewModelScope.launch {
            if (ym != AppClock.currentYearMonth()) {
                nextMonthWarning.value = null
                return@launch
            }
            val items = listNextMonthPeriodics(ym)
            nextMonthWarning.value = when {
                items.isEmpty() -> null
                items.size == 1 -> {
                    val f = items.first()
                    "À prévoir le mois prochain : ${f.label} (" +
                        "${MoneyFormat.formatCents(f.amountCents)} le ${f.dayOfMonth}). " +
                        "Voir dans Réglages"
                }
                else -> {
                    val f = items.first()
                    val others = items.size - 1
                    val otherWord = if (others == 1) "autre" else "autres"
                    "À prévoir le mois prochain : ${f.label} (" +
                        "${MoneyFormat.formatCents(f.amountCents)} le ${f.dayOfMonth}) " +
                        "et $others $otherWord. Voir dans Réglages"
                }
            }
        }
    }

    fun prevMonth() {
        viewModelScope.launch {
            val current = AppClock.currentYearMonth()
            if (!MonthNavigation.canGoPrev(selectedMonth.value, current)) return@launch
            val target = selectedMonth.value.minusMonths(1)
            monthCycleEngine.ensureMonthExists(target)
            selectedMonth.value = target
        }
    }

    fun nextMonth() {
        viewModelScope.launch {
            val target = selectedMonth.value.plusMonths(1)
            val current = AppClock.currentYearMonth()
            if (!MonthNavigation.canGoNext(selectedMonth.value, current)) return@launch
            monthCycleEngine.ensureMonthExists(target)
            selectedMonth.value = target
        }
    }

    /** Retour rapide au mois calendaire courant (navigation année / historique). */
    fun goToCurrentMonth() {
        goToMonth(AppClock.currentYearMonth())
    }

    /** Saut depuis le calendrier Menu (passé, courant, ou aperçu futur proche). */
    fun goToMonth(target: java.time.YearMonth) {
        viewModelScope.launch {
            val current = AppClock.currentYearMonth()
            if (!MonthNavigation.canSelect(target, current)) return@launch
            monthCycleEngine.ensureMonthExists(target)
            selectedMonth.value = target
        }
    }

    fun requestToggleDone(expenseId: String) {
        val ym = selectedMonth.value
        val current = AppClock.currentYearMonth()
        if (MonthNavigation.isFuturePreview(ym, current)) {
            _userMessages.tryEmit("Pas de FAIT sur un mois à venir — ouvrez le mois en cours.")
            return
        }
        if (ym.isBefore(current)) {
            pendingPastToggleId.value = expenseId
        } else {
            performToggle(expenseId)
        }
    }

    fun confirmPastToggle() {
        val id = pendingPastToggleId.value ?: return
        pendingPastToggleId.value = null
        performToggle(id)
    }

    fun dismissPastToggle() {
        pendingPastToggleId.value = null
    }

    /** Annuler après FAIT/Remettre — sans redemander le dialogue « mois passé ». */
    fun undoToggleDone(expenseId: String) {
        performToggle(expenseId, fromUndo = true)
    }

    private val toggleInFlight = mutableSetOf<String>()
    private val toggleUndoQueued = mutableSetOf<String>()

    private fun performToggle(expenseId: String, fromUndo: Boolean = false) {
        if (expenseId in toggleInFlight) {
            if (fromUndo) toggleUndoQueued.add(expenseId)
            return
        }
        toggleInFlight.add(expenseId)
        viewModelScope.launch {
            try {
                when (toggleDone.invoke(expenseId)) {
                    is ToggleExpenseDoneResult.FuturePreviewBlocked ->
                        _userMessages.tryEmit(
                            "Pas de FAIT sur un mois à venir — ouvrez le mois en cours."
                        )
                    else -> Unit
                }
                if (toggleUndoQueued.remove(expenseId)) {
                    when (toggleDone.invoke(expenseId)) {
                        is ToggleExpenseDoneResult.FuturePreviewBlocked ->
                            _userMessages.tryEmit(
                                "Pas de FAIT sur un mois à venir — ouvrez le mois en cours."
                            )
                        else -> Unit
                    }
                }
            } finally {
                toggleInFlight.remove(expenseId)
                toggleUndoQueued.remove(expenseId)
            }
        }
    }

    fun setHighlightExpenseId(id: String?) {
        highlightExpenseId.value = id
        if (id != null) {
            viewModelScope.launch {
                val exp = instanceDao.getExpenseById(id) ?: return@launch
                val ym = runCatching {
                    java.time.YearMonth.parse(exp.ledgerId)
                }.getOrNull() ?: return@launch
                val current = AppClock.currentYearMonth()
                if (!MonthNavigation.canSelect(ym, current)) return@launch
                monthCycleEngine.ensureMonthExists(ym)
                selectedMonth.value = ym
            }
        }
    }

    fun clearHighlight() {
        highlightExpenseId.value = null
    }

    fun addDivers(eurosText: String) {
        adjustDiversBy(eurosText, add = true)
    }

    fun removeDivers(eurosText: String) {
        adjustDiversBy(eurosText, add = false)
    }

    private fun adjustDiversBy(eurosText: String, add: Boolean) {
        if (_diversBusy.value) return
        val current = AppClock.currentYearMonth()
        if (MonthNavigation.isFuturePreview(selectedMonth.value, current)) {
            _userMessages.tryEmit("Les courses se notent sur le mois en cours.")
            return
        }
        _diversBusy.value = true
        viewModelScope.launch {
            try {
                if (MoneyFormat.tooManyFractionDigits(eurosText)) {
                    _diversError.value = MoneyFormat.FRACTION_HINT
                    return@launch
                }
                val cents = MoneyFormat.parseToCents(eurosText)
                if (cents == null || cents <= 0L) {
                    _diversError.value = "Indiquez un montant valide (ex. 12,50)."
                    return@launch
                }
                if (MoneyFormat.isOverLineMax(cents)) {
                    _diversError.value = MoneyFormat.LINE_MAX_HINT
                    return@launch
                }
                val delta = if (add) cents else -cents
                val ledgerId = AppClock.ledgerId(selectedMonth.value)
                when (val result = adjustDivers(ledgerId, delta)) {
                    is AdjustDiversResult.Success -> {
                        _diversError.value = null
                        _diversFieldCleared.tryEmit(Unit)
                    }
                    is AdjustDiversResult.ReducedToZero -> {
                        // Succès (clamp à 0) — message informatif, pas canal erreur rouge.
                        _diversError.value = null
                        _diversFieldCleared.tryEmit(Unit)
                        _userMessages.tryEmit(
                            if (result.previousCents <= 0L) {
                                "Il n’y a pas de courses notées."
                            } else {
                                "Il n’y a que ${MoneyFormat.formatCents(result.previousCents)} en courses. Tout a été retiré."
                            }
                        )
                    }
                    is AdjustDiversResult.Failed -> _diversError.value = result.message
                }
            } finally {
                _diversBusy.value = false
            }
        }
    }

    fun clearDiversError() {
        _diversError.value = null
    }

    fun clearSavingsError() {
        _savingsError.value = null
    }

    /** Forfait « Mettre de côté » depuis le sticky (jour conservé / défaut 1). */
    fun saveSavingsFromHome(eurosText: String) {
        if (_savingsBusy.value) return
        val current = AppClock.currentYearMonth()
        if (MonthNavigation.isFuturePreview(selectedMonth.value, current)) {
            _userMessages.tryEmit("L’épargne se règle sur le mois en cours.")
            return
        }
        if (MoneyFormat.tooManyFractionDigits(eurosText)) {
            _savingsError.value = MoneyFormat.FRACTION_HINT
            return
        }
        val cents = MoneyFormat.parseToCents(eurosText)
        if (cents == null || cents <= 0L) {
            _savingsError.value = "Indiquez un montant à mettre de côté."
            return
        }
        if (MoneyFormat.isOverLineMax(cents)) {
            _savingsError.value = MoneyFormat.LINE_MAX_HINT
            return
        }
        _savingsBusy.value = true
        _savingsError.value = null
        viewModelScope.launch {
            try {
                val day = configureSavings.read().dayOfMonth.coerceIn(1, 31)
                when (val result = configureSavings.save(cents, day)) {
                    ConfigureSavingsResult.Saved -> {
                        _savingsError.value = null
                        _userMessages.tryEmit("Mettre de côté noté.")
                    }
                    is ConfigureSavingsResult.InvalidInput ->
                        _savingsError.value = result.message
                    ConfigureSavingsResult.Removed -> Unit
                }
            } finally {
                _savingsBusy.value = false
            }
        }
    }

    /** Contact Menu → Arkalia Système (mailto). */
    fun writeSupportMail() {
        viewModelScope.launch {
            val pm = appContext.packageManager
            val pkg = appContext.packageName
            val info = runCatching {
                if (Build.VERSION.SDK_INT >= 33) {
                    pm.getPackageInfo(pkg, android.content.pm.PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(pkg, 0)
                }
            }.getOrNull()
            val versionName = info?.versionName ?: "?"
            val versionCode = if (info != null) {
                if (Build.VERSION.SDK_INT >= 28) info.longVersionCode.toInt() else {
                    @Suppress("DEPRECATION")
                    info.versionCode
                }
            } else {
                0
            }
            val draft = SupportMailComposer.build(
                versionName = versionName,
                versionCode = versionCode,
                deviceModel = Build.MODEL.orEmpty(),
                monthLabel = DayLabelFormat.monthTitle(selectedMonth.value)
            )
            val intent = Intent(
                Intent.ACTION_SENDTO,
                Uri.parse(SupportMailComposer.mailtoUri(draft))
            )
            _shareIntents.emit(
                Intent.createChooser(
                    intent,
                    "Écrire à ${SupportMailComposer.SUPPORT_DISPLAY_NAME}"
                )
            )
        }
    }

    fun copyPaymentNote(note: String) {
        val prepared = BelgianStructuredCommunication.prepareCopy(note)
        if (prepared.clipboardText.isEmpty()) {
            _userMessages.tryEmit(prepared.userMessage)
            return
        }
        SensitiveClipboard.setPlainText(appContext, "Communication", prepared.clipboardText)
        _userMessages.tryEmit(prepared.userMessage)
    }

    /** Décode la photo **dans l’app** (S01) — pas via `ACTION_VIEW` externe. */
    suspend fun loadReceiptBitmap(relativePath: String): Bitmap? =
        receiptPhotos.decodeBitmap(relativePath)

    fun isPdfReceipt(relativePath: String): Boolean = receiptPhotos.isPdf(relativePath)

    /** Photo d’abord si présente, sinon premier fichier (PDF seul). */
    fun primaryReceiptPath(raw: String): String =
        ReceiptPathList.primaryPhotoOrFirst(raw.trim(), receiptPhotos::isPdf)

    /** PDF (et repli photo) : lecteur système. */
    suspend fun viewReceiptIntent(relativePath: String): Intent? {
        val path = relativePath.trim()
        if (path.isEmpty()) return null
        val uri = receiptPhotos.viewUri(path) ?: return null
        return receiptPhotos.viewIntent(uri, path)
    }

    fun refreshCurrentMonth() {
        viewModelScope.launch {
            monthCycleEngine.ensureCurrentMonthExists()
            selectedMonth.update { AppClock.currentYearMonth() }
        }
    }

    /** B10 : au resume, si le calendrier a basculé et qu’on affichait l’ancien « courant ». */
    fun refreshCurrentMonthIfRolled() {
        val calendar = AppClock.currentYearMonth()
        if (calendar == lastCalendarMonth) return
        val wasOnPreviousCurrent = selectedMonth.value == lastCalendarMonth
        lastCalendarMonth = calendar
        if (wasOnPreviousCurrent) {
            refreshCurrentMonth()
        }
    }
}
