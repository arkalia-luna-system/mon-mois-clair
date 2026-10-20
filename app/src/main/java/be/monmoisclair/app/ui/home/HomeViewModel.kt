package be.monmoisclair.app.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.MonthLedgerDao
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.data.reminder.ReminderHealthChecker
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.ExpenseOverdue
import be.monmoisclair.app.domain.MonthCycleEngine
import be.monmoisclair.app.domain.MonthNavigation
import be.monmoisclair.app.domain.MonthSummary
import be.monmoisclair.app.domain.MonthSummaryCalculator
import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.domain.usecase.AdjustDiversResult
import be.monmoisclair.app.domain.usecase.AdjustDiversUseCase
import be.monmoisclair.app.domain.usecase.ListNextMonthPeriodicsUseCase
import be.monmoisclair.app.domain.usecase.ToggleExpenseDoneUseCase
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
    val canGoPrev: Boolean = false,
    val canGoNext: Boolean = false,
    val summary: MonthSummary = MonthSummary(0, 0, 0, 0),
    val diversSpentCents: Long = 0L,
    val afterDiversCents: Long = 0L,
    val timeline: List<TimelineItem> = emptyList(),
    val highlightExpenseId: String? = null,
    /** Demande confirm FAIT sur mois passé (T4). */
    val pendingPastToggleId: String? = null,
    val nextMonthWarning: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val monthCycleEngine: MonthCycleEngine,
    private val monthLedgerDao: MonthLedgerDao,
    private val instanceDao: InstanceDao,
    private val toggleDone: ToggleExpenseDoneUseCase,
    private val adjustDivers: AdjustDiversUseCase,
    private val listNextMonthPeriodics: ListNextMonthPeriodicsUseCase,
    private val reminderHealth: ReminderHealthChecker,
    private val receiptPhotos: ReceiptPhotoStore
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(AppClock.currentYearMonth())
    private val highlightExpenseId = MutableStateFlow<String?>(null)
    private val pendingPastToggleId = MutableStateFlow<String?>(null)
    private val nextMonthWarning = MutableStateFlow<String?>(null)
    private val _diversError = MutableStateFlow<String?>(null)
    val diversError: StateFlow<String?> = _diversError.asStateFlow()
    private val _remindersBlocked = MutableStateFlow(false)
    val remindersBlocked: StateFlow<Boolean> = _remindersBlocked.asStateFlow()
    private val _diversFieldCleared = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val diversFieldCleared: SharedFlow<Unit> = _diversFieldCleared.asSharedFlow()
    private val _userMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val userMessages: SharedFlow<String> = _userMessages.asSharedFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        selectedMonth.flatMapLatest { ym ->
            val ledgerId = AppClock.ledgerId(ym)
            combine(
                instanceDao.observeIncomes(ledgerId),
                instanceDao.observeExpenses(ledgerId),
                monthLedgerDao.observeAll(),
                highlightExpenseId,
                pendingPastToggleId
            ) { incomes, expenses, ledgers, highlight, pendingToggle ->
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
                                    status = exp.status
                                ),
                                isAutoDebit = exp.isAutoDebit,
                                paymentNote = exp.paymentNote,
                                hasReceipt = exp.receiptRelativePath.isNotBlank(),
                                receiptRelativePath = exp.receiptRelativePath,
                                sortDay = exp.dayOfMonth,
                                sortLabel = exp.label
                            )
                        )
                    }
                }.sortedWith(compareBy({ it.sortDay }, { it.sortLabel }))

                val current = AppClock.currentYearMonth()
                HomeUiState(
                    loading = false,
                    ledgerId = ledgerId,
                    selectedYearMonth = ym,
                    monthLabel = DayLabelFormat.monthTitle(ym),
                    isPastMonth = ym.isBefore(current),
                    canGoPrev = MonthNavigation.canGoPrev(),
                    canGoNext = MonthNavigation.canGoNext(ym, current),
                    summary = summary,
                    diversSpentCents = divers,
                    afterDiversCents = summary.remainingCents - divers,
                    timeline = timeline,
                    highlightExpenseId = highlight,
                    pendingPastToggleId = pendingToggle
                )
            }
        },
        nextMonthWarning
    ) { base, warning -> base.copy(nextMonthWarning = warning) }
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
                        "${MoneyFormat.formatCents(f.amountCents)} le ${f.dayOfMonth})"
                }
                else -> {
                    val f = items.first()
                    val others = items.size - 1
                    val otherWord = if (others == 1) "autre" else "autres"
                    "À prévoir le mois prochain : ${f.label} (" +
                        "${MoneyFormat.formatCents(f.amountCents)} le ${f.dayOfMonth}) " +
                        "et $others $otherWord"
                }
            }
        }
    }

    fun prevMonth() {
        viewModelScope.launch {
            val target = selectedMonth.value.minusMonths(1)
            monthCycleEngine.ensureMonthExists(target)
            selectedMonth.value = target
        }
    }

    fun nextMonth() {
        viewModelScope.launch {
            val target = selectedMonth.value.plusMonths(1)
            val current = AppClock.currentYearMonth()
            if (target > current) return@launch
            monthCycleEngine.ensureMonthExists(target)
            selectedMonth.value = target
        }
    }

    /** Retour rapide au mois calendaire courant (navigation année / historique). */
    fun goToCurrentMonth() {
        goToMonth(AppClock.currentYearMonth())
    }

    /** Saut depuis le calendrier Menu (jamais vers le futur). */
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
        if (ym.isBefore(AppClock.currentYearMonth())) {
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

    private fun performToggle(expenseId: String) {
        viewModelScope.launch {
            toggleDone.invoke(expenseId)
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
        viewModelScope.launch {
            val cents = MoneyFormat.parseToCents(eurosText)
            if (cents == null || cents <= 0L) {
                _diversError.value = "Indiquez un montant valide (ex. 12,50)."
                return@launch
            }
            val delta = if (add) cents else -cents
            val ledgerId = AppClock.ledgerId(selectedMonth.value)
            when (val result = adjustDivers(ledgerId, delta)) {
                is AdjustDiversResult.Success -> {
                    _diversError.value = null
                    _diversFieldCleared.tryEmit(Unit)
                }
                is AdjustDiversResult.Failed -> _diversError.value = result.message
            }
        }
    }

    fun clearDiversError() {
        _diversError.value = null
    }

    fun copyPaymentNote(note: String) {
        val trimmed = note.trim()
        if (trimmed.isEmpty()) {
            _userMessages.tryEmit("Aucune communication à copier.")
            return
        }
        (appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText("communication", trimmed))
        _userMessages.tryEmit("Copié — collez dans votre application bancaire.")
    }

    /**
     * Décode la photo **dans l’app** (S01) — pas via `ACTION_VIEW` externe
     * (Samsung refuse souvent la lecture SAF hors process).
     */
    suspend fun loadReceiptBitmap(relativePath: String): Bitmap? {
        val uri = receiptPhotos.openUri(relativePath) ?: return null
        return runCatching {
            appContext.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    }

    fun refreshCurrentMonth() {
        viewModelScope.launch {
            monthCycleEngine.ensureCurrentMonthExists()
            selectedMonth.update { AppClock.currentYearMonth() }
        }
    }
}
