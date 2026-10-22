package be.monmoisclair.app.ui.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.domain.usecase.ListUpcomingMonthsUseCase
import be.monmoisclair.app.domain.usecase.UpcomingMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpcomingUiState(
    val loading: Boolean = true,
    val months: List<UpcomingMonth> = emptyList()
)

@HiltViewModel
class UpcomingViewModel @Inject constructor(
    private val listUpcoming: ListUpcomingMonthsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(UpcomingUiState())
    val state: StateFlow<UpcomingUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val previous = _state.value
            if (previous.months.isEmpty()) {
                _state.update { it.copy(loading = true) }
            }
            val months = listUpcoming()
            _state.update { it.copy(loading = false, months = months) }
        }
    }
}
