package be.monmoisclair.app.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.domain.AppClock
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

data class EntryFormState(
    val label: String = "",
    val amountText: String = "",
    val dayText: String = AppClock.today().dayOfMonth.toString(),
    val isExpense: Boolean = true,
    val recurring: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val saving: Boolean = false
)

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    private val addEntry: AddEntryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EntryFormState())
    val state: StateFlow<EntryFormState> = _state.asStateFlow()

    fun setLabel(v: String) = _state.update { it.copy(label = v, error = null) }
    fun setAmount(v: String) = _state.update { it.copy(amountText = v, error = null) }
    fun setDay(v: String) = _state.update { it.copy(dayText = v, error = null) }
    fun setExpense(v: Boolean) = _state.update { it.copy(isExpense = v) }
    fun setRecurring(v: Boolean) = _state.update { it.copy(recurring = v) }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            val cents = MoneyFormat.parseToCents(s.amountText)
            val day = s.dayText.trim().toIntOrNull()
            if (cents == null || day == null) {
                _state.update { it.copy(error = "Vérifiez le montant et le jour (1 à 31).") }
                return@launch
            }
            _state.update { it.copy(saving = true, error = null) }
            when (val result = addEntry(
                label = s.label,
                amountCents = cents,
                dayOfMonth = day,
                isExpense = s.isExpense,
                recurring = s.recurring
            )) {
                is AddEntryResult.Success ->
                    _state.update { it.copy(saving = false, saved = true) }
                is AddEntryResult.InvalidInput ->
                    _state.update { it.copy(saving = false, error = result.message) }
            }
        }
    }
}
