package be.monmoisclair.app.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.RecurrenceAnchor
import be.monmoisclair.app.domain.usecase.AddEntryResult
import be.monmoisclair.app.domain.usecase.AddEntryUseCase
import be.monmoisclair.app.ui.format.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RecurrenceChoice {
    Once,
    Monthly,
    Every3Months,
    Yearly
}

data class EntryFormState(
    val label: String = "",
    val amountText: String = "",
    val dayText: String = AppClock.today().dayOfMonth.toString(),
    val isExpense: Boolean = true,
    val recurrence: RecurrenceChoice = RecurrenceChoice.Once,
    /** Mois-calendaire 1–12 pour la prochaine échéance (T6). */
    val nextDueMonth: Int = AppClock.currentYearMonth().monthValue,
    val isAutoDebit: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val saving: Boolean = false
) {
    val needsNextDueMonth: Boolean
        get() = recurrence == RecurrenceChoice.Every3Months ||
            recurrence == RecurrenceChoice.Yearly
}

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    private val addEntry: AddEntryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EntryFormState())
    val state: StateFlow<EntryFormState> = _state.asStateFlow()

    fun setLabel(v: String) = _state.update { it.copy(label = v, error = null) }
    fun setAmount(v: String) = _state.update { it.copy(amountText = v, error = null) }
    fun setDay(v: String) = _state.update { it.copy(dayText = v, error = null) }
    fun setExpense(v: Boolean) = _state.update {
        it.copy(isExpense = v, isAutoDebit = if (v) it.isAutoDebit else false)
    }
    fun setRecurrence(v: RecurrenceChoice) = _state.update { it.copy(recurrence = v) }
    fun setNextDueMonth(month: Int) = _state.update {
        it.copy(nextDueMonth = month.coerceIn(1, 12))
    }
    fun setAutoDebit(v: Boolean) = _state.update { it.copy(isAutoDebit = v) }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            val cents = MoneyFormat.parseToCents(s.amountText)
            val day = s.dayText.trim().toIntOrNull()
            if (cents == null || day == null) {
                _state.update { it.copy(error = "Vérifiez le montant et le jour (1 à 31).") }
                return@launch
            }
            val recurring = s.recurrence != RecurrenceChoice.Once
            val interval = when (s.recurrence) {
                RecurrenceChoice.Once, RecurrenceChoice.Monthly -> 1
                RecurrenceChoice.Every3Months -> 3
                RecurrenceChoice.Yearly -> 12
            }
            val anchor = when {
                !recurring -> null
                s.needsNextDueMonth ->
                    RecurrenceAnchor.nextYearMonth(s.nextDueMonth)
                else -> AppClock.currentYearMonth()
            }
            _state.update { it.copy(saving = true, error = null) }
            when (
                val result = addEntry(
                    label = s.label,
                    amountCents = cents,
                    dayOfMonth = day,
                    isExpense = s.isExpense,
                    recurring = recurring,
                    intervalMonths = interval,
                    anchorYearMonth = anchor,
                    isAutoDebit = s.isExpense && s.isAutoDebit
                )
            ) {
                is AddEntryResult.Success ->
                    _state.update { it.copy(saving = false, saved = true) }
                is AddEntryResult.InvalidInput ->
                    _state.update { it.copy(saving = false, error = result.message) }
            }
        }
    }
}
