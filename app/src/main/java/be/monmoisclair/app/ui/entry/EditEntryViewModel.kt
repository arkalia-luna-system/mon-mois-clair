package be.monmoisclair.app.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.domain.usecase.DeleteInstanceResult
import be.monmoisclair.app.domain.usecase.DeleteInstanceUseCase
import be.monmoisclair.app.domain.usecase.UpdateInstanceResult
import be.monmoisclair.app.domain.usecase.UpdateInstanceUseCase
import be.monmoisclair.app.ui.format.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditEntryState(
    val id: String = "",
    val isExpense: Boolean = true,
    val label: String = "",
    val amountText: String = "",
    val dayText: String = "1",
    val isAutoDebit: Boolean = false,
    val recurrenceInfo: String? = null,
    val hasTemplate: Boolean = false,
    val applyToFollowing: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
    val loading: Boolean = true
)

@HiltViewModel
class EditEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val instanceDao: InstanceDao,
    private val templateDao: TemplateDao,
    private val updateInstance: UpdateInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase
) : ViewModel() {

    private val entryId: String = checkNotNull(savedStateHandle["id"])
    private val isExpenseArg: Boolean = savedStateHandle.get<String>("isExpense") == "true"

    private val _state = MutableStateFlow(EditEntryState(id = entryId, isExpense = isExpenseArg))
    val state: StateFlow<EditEntryState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        if (isExpenseArg) {
            val e = instanceDao.getExpenseById(entryId)
            if (e == null) {
                _state.update { it.copy(loading = false, error = "Entrée introuvable.") }
                return
            }
            val euros = e.amountCents / 100
            val rem = (e.amountCents % 100).toInt()
            val amountText = if (rem == 0) euros.toString() else "%d,%02d".format(euros, rem)
            val recurrenceInfo = e.templateId?.let { tid ->
                templateDao.getExpenseById(tid)?.let { tpl ->
                    RecurrenceLabel.forIntervalMonths(tpl.intervalMonths)
                }
            } ?: RecurrenceLabel.oneOff()
            _state.update {
                it.copy(
                    loading = false,
                    label = e.label,
                    amountText = amountText,
                    dayText = e.dayOfMonth.toString(),
                    isAutoDebit = e.isAutoDebit,
                    recurrenceInfo = recurrenceInfo,
                    hasTemplate = e.templateId != null
                )
            }
        } else {
            val e = instanceDao.getIncomeById(entryId)
            if (e == null) {
                _state.update { it.copy(loading = false, error = "Entrée introuvable.") }
                return
            }
            val euros = e.amountCents / 100
            val rem = (e.amountCents % 100).toInt()
            val amountText = if (rem == 0) euros.toString() else "%d,%02d".format(euros, rem)
            val recurrenceInfo = if (e.templateId != null) {
                RecurrenceLabel.forIntervalMonths(1)
            } else {
                RecurrenceLabel.oneOff()
            }
            _state.update {
                it.copy(
                    loading = false,
                    label = e.label,
                    amountText = amountText,
                    dayText = e.dayOfMonth.toString(),
                    recurrenceInfo = recurrenceInfo,
                    hasTemplate = e.templateId != null
                )
            }
        }
    }

    fun setLabel(v: String) = _state.update { it.copy(label = v, error = null) }
    fun setAmount(v: String) = _state.update { it.copy(amountText = v, error = null) }
    fun setDay(v: String) = _state.update { it.copy(dayText = v, error = null) }
    fun setAutoDebit(v: Boolean) = _state.update { it.copy(isAutoDebit = v, error = null) }
    fun setApplyToFollowing(v: Boolean) = _state.update { it.copy(applyToFollowing = v) }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            val cents = MoneyFormat.parseToCents(s.amountText)
            val day = s.dayText.trim().toIntOrNull()
            if (cents == null || day == null) {
                _state.update { it.copy(error = "Vérifiez le montant et le jour (1 à 31).") }
                return@launch
            }
            when (
                val result = updateInstance(
                    id = s.id,
                    isExpense = s.isExpense,
                    label = s.label,
                    amountCents = cents,
                    dayOfMonth = day,
                    applyToFollowingMonths = s.applyToFollowing,
                    isAutoDebit = s.isExpense && s.isAutoDebit
                )
            ) {
                UpdateInstanceResult.Success -> _state.update { it.copy(done = true) }
                UpdateInstanceResult.NotFound ->
                    _state.update { it.copy(error = "Entrée introuvable.") }
                is UpdateInstanceResult.InvalidInput ->
                    _state.update { it.copy(error = result.message) }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            val s = _state.value
            when (
                deleteInstance(
                    id = s.id,
                    isExpense = s.isExpense,
                    applyToFollowingMonths = s.applyToFollowing
                )
            ) {
                DeleteInstanceResult.Success -> _state.update { it.copy(done = true) }
                DeleteInstanceResult.NotFound ->
                    _state.update { it.copy(error = "Entrée introuvable.") }
            }
        }
    }
}
