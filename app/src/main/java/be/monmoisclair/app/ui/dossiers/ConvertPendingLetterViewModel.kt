package be.monmoisclair.app.ui.dossiers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.dao.PendingLetterDao
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.usecase.ConvertPendingLetterResult
import be.monmoisclair.app.domain.usecase.ConvertPendingLetterToDossierUseCase
import be.monmoisclair.app.ui.SingleFlightGate
import be.monmoisclair.app.ui.format.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConvertPendingLetterUiState(
    val loading: Boolean = true,
    val who: String = "",
    val what: String = "",
    val openingRemainingText: String = "",
    val monthlyAmountText: String = "",
    val dayText: String = AppClock.today().dayOfMonth.toString(),
    val error: String? = null,
    val saving: Boolean = false,
    val done: Boolean = false,
    val createdInstanceId: String? = null
)

@HiltViewModel
class ConvertPendingLetterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pendingLetterDao: PendingLetterDao,
    private val convert: ConvertPendingLetterToDossierUseCase
) : ViewModel() {
    private val letterId: String = checkNotNull(savedStateHandle["letterId"])

    private val _state = MutableStateFlow(ConvertPendingLetterUiState())
    val state: StateFlow<ConvertPendingLetterUiState> = _state.asStateFlow()
    private val convertGate = SingleFlightGate()

    init {
        viewModelScope.launch {
            val letter = pendingLetterDao.getById(letterId)
            if (letter == null) {
                _state.update {
                    it.copy(loading = false, error = "Ce courrier n’est plus là.")
                }
                return@launch
            }
            _state.update {
                it.copy(
                    loading = false,
                    who = letter.who,
                    what = letter.what
                )
            }
        }
    }

    fun setOpeningRemaining(v: String) = _state.update {
        it.copy(openingRemainingText = v, error = null)
    }

    fun setMonthlyAmount(v: String) = _state.update {
        it.copy(monthlyAmountText = v, error = null)
    }

    fun setDay(v: String) = _state.update { it.copy(dayText = v, error = null) }

    fun convert() {
        if (_state.value.saving || _state.value.done || _state.value.loading) return
        if (!convertGate.tryEnter()) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
            val s = _state.value
            if (MoneyFormat.tooManyFractionDigits(s.openingRemainingText) ||
                MoneyFormat.tooManyFractionDigits(s.monthlyAmountText)
            ) {
                _state.update { it.copy(saving = false, error = MoneyFormat.FRACTION_HINT) }
                return@launch
            }
            val opening = MoneyFormat.parseToCents(s.openingRemainingText)
            val monthly = MoneyFormat.parseToCents(s.monthlyAmountText)
            val day = s.dayText.trim().toIntOrNull()
            if (opening == null || monthly == null || day == null) {
                _state.update {
                    it.copy(
                        saving = false,
                        error = "Vérifiez le reste dû, le versement et le jour."
                    )
                }
                return@launch
            }
            if (MoneyFormat.isOverOpeningMax(opening)) {
                _state.update {
                    it.copy(saving = false, error = MoneyFormat.OPENING_MAX_HINT)
                }
                return@launch
            }
            if (MoneyFormat.isOverLineMax(monthly)) {
                _state.update {
                    it.copy(saving = false, error = MoneyFormat.LINE_MAX_HINT)
                }
                return@launch
            }
            when (
                val result = convert(
                    letterId = letterId,
                    monthlyAmountCents = monthly,
                    openingRemainingCents = opening,
                    dayOfMonth = day
                )
            ) {
                is ConvertPendingLetterResult.Success ->
                    _state.update {
                        it.copy(
                            saving = false,
                            done = true,
                            createdInstanceId = result.instanceId
                        )
                    }
                is ConvertPendingLetterResult.InvalidInput ->
                    _state.update { it.copy(saving = false, error = result.message) }
                ConvertPendingLetterResult.NotFound ->
                    _state.update {
                        it.copy(saving = false, error = "Ce courrier n’est plus là.")
                    }
            }
            } finally {
                convertGate.exit()
            }
        }
    }
}
