package be.monmoisclair.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.MonthLedgerDao
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.MonthCycleEngine
import be.monmoisclair.app.domain.MonthSummary
import be.monmoisclair.app.domain.MonthSummaryCalculator
import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.domain.usecase.ToggleExpenseDoneUseCase
import be.monmoisclair.app.ui.format.DayLabelFormat
import be.monmoisclair.app.ui.format.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val ledgerId: String = "",
    val monthLabel: String = "",
    val canGoPrev: Boolean = false,
    val canGoNext: Boolean = false,
    val summary: MonthSummary = MonthSummary(0, 0, 0, 0),
    val timeline: List<TimelineItem> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val monthCycleEngine: MonthCycleEngine,
    private val monthLedgerDao: MonthLedgerDao,
    private val instanceDao: InstanceDao,
    private val toggleDone: ToggleExpenseDoneUseCase
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(AppClock.currentYearMonth())

    val uiState: StateFlow<HomeUiState> = selectedMonth
        .flatMapLatest { ym ->
            val ledgerId = AppClock.ledgerId(ym)
            combine(
                instanceDao.observeIncomes(ledgerId),
                instanceDao.observeExpenses(ledgerId),
                monthLedgerDao.observeAll()
            ) { incomes, expenses, ledgers ->
                val summary = MonthSummaryCalculator.compute(incomes, expenses)
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
                                sortDay = exp.dayOfMonth,
                                sortLabel = exp.label
                            )
                        )
                    }
                }.sortedWith(compareBy({ it.sortDay }, { it.sortLabel }))

                val ledgerIds = ledgers.map { it.id }.toSet()
                val current = AppClock.currentYearMonth()
                val prev = ym.minusMonths(1)
                val next = ym.plusMonths(1)
                HomeUiState(
                    loading = false,
                    ledgerId = ledgerId,
                    monthLabel = DayLabelFormat.monthTitle(ym),
                    // Mois passés : navigation seulement s'ils existent déjà en base.
                    canGoPrev = ledgerIds.contains(AppClock.ledgerId(prev)),
                    canGoNext = next <= current,
                    summary = summary,
                    timeline = timeline
                )
            }
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
    }

    fun prevMonth() {
        viewModelScope.launch {
            val target = selectedMonth.value.minusMonths(1)
            // Ne crée pas automatiquement un mois passé absent (conforme charte),
            // sauf s'il existe déjà en base.
            selectedMonth.value = target
        }
    }

    fun nextMonth() {
        viewModelScope.launch {
            val target = selectedMonth.value.plusMonths(1)
            val current = AppClock.currentYearMonth()
            if (target == current) {
                monthCycleEngine.ensureCurrentMonthExists()
            } else if (target > current) {
                return@launch
            }
            selectedMonth.value = target
        }
    }

    fun toggleDone(expenseId: String) {
        viewModelScope.launch {
            toggleDone.invoke(expenseId)
        }
    }

    fun refreshCurrentMonth() {
        viewModelScope.launch {
            monthCycleEngine.ensureCurrentMonthExists()
            selectedMonth.update { AppClock.currentYearMonth() }
        }
    }
}
