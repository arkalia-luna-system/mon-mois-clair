package be.monmoisclair.app.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.RecurrenceAnchor
import be.monmoisclair.app.domain.usecase.UpdateExpenseTemplateResult
import be.monmoisclair.app.domain.usecase.UpdateExpenseTemplateUseCase
import be.monmoisclair.app.ui.format.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class EditTemplateState(
    val label: String = "",
    val amountText: String = "",
    val dayText: String = "1",
    val anchorMonth: Int = AppClock.currentYearMonth().monthValue,
    /** Ancre complète en base — pour ne pas recalculer l’année si le mois ne change pas. */
    val existingAnchor: YearMonth? = null,
    val intervalHint: String = "",
    val error: String? = null,
    val loading: Boolean = true,
    val saved: Boolean = false,
    val saving: Boolean = false
) {
    val anchorYearLabel: String
        get() {
            val resolved = RecurrenceAnchor.resolveTemplateAnchor(
                selectedMonth = anchorMonth,
                existingAnchor = existingAnchor
            )
            return "${RecurrenceAnchor.frenchMonthLabel(resolved.monthValue)} ${resolved.year}"
        }
}

@HiltViewModel
class EditTemplateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val templateDao: TemplateDao,
    private val updateTemplate: UpdateExpenseTemplateUseCase
) : ViewModel() {
    private val templateId: String = checkNotNull(savedStateHandle["id"])
    private val _state = MutableStateFlow(EditTemplateState())
    val state: StateFlow<EditTemplateState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val tpl = templateDao.getExpenseById(templateId) ?: run {
            _state.update { it.copy(loading = false, error = "Échéance introuvable.") }
            return
        }
        val rem = (tpl.amountCents % 100).toInt()
        val amountText = if (rem == 0) {
            (tpl.amountCents / 100).toString()
        } else {
            "%d,%02d".format(tpl.amountCents / 100, rem)
        }
        val anchor = runCatching { YearMonth.parse(tpl.anchorYearMonth) }.getOrNull()
            ?: AppClock.currentYearMonth()
        val hint = when (tpl.intervalMonths) {
            12 -> "1× par an — les mois suivants suivront cette ancre."
            3 -> "Tous les 3 mois — les mois suivants suivront cette ancre."
            else -> "Tous les ${tpl.intervalMonths} mois — les mois suivants suivront cette ancre."
        }
        _state.update {
            it.copy(
                loading = false,
                label = tpl.label,
                amountText = amountText,
                dayText = tpl.dayOfMonth.toString(),
                anchorMonth = anchor.monthValue,
                existingAnchor = anchor,
                intervalHint = hint
            )
        }
    }

    fun setLabel(v: String) = _state.update { it.copy(label = v, error = null) }
    fun setAmount(v: String) = _state.update { it.copy(amountText = v, error = null) }
    fun setDay(v: String) = _state.update { it.copy(dayText = v, error = null) }
    fun setAnchorMonth(m: Int) = _state.update { it.copy(anchorMonth = m.coerceIn(1, 12)) }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            val cents = MoneyFormat.parseToCents(s.amountText)
            val day = s.dayText.trim().toIntOrNull()
            if (cents == null || day == null) {
                _state.update { it.copy(error = "Vérifiez le montant et le jour (1 à 31).") }
                return@launch
            }
            val anchor = RecurrenceAnchor.resolveTemplateAnchor(
                selectedMonth = s.anchorMonth,
                existingAnchor = s.existingAnchor
            )
            _state.update { it.copy(saving = true, error = null) }
            when (
                val result = updateTemplate(
                    templateId = templateId,
                    label = s.label,
                    amountCents = cents,
                    dayOfMonth = day,
                    anchorYearMonth = anchor
                )
            ) {
                UpdateExpenseTemplateResult.Success ->
                    _state.update { it.copy(saving = false, saved = true) }
                UpdateExpenseTemplateResult.NotFound ->
                    _state.update { it.copy(saving = false, error = "Échéance introuvable.") }
                is UpdateExpenseTemplateResult.InvalidInput ->
                    _state.update { it.copy(saving = false, error = result.message) }
            }
        }
    }
}
