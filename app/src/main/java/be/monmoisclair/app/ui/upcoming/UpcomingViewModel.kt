package be.monmoisclair.app.ui.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.domain.usecase.ListUpcomingMonthsUseCase
import be.monmoisclair.app.domain.usecase.UpcomingMonth
import be.monmoisclair.app.ui.AsyncLoadCoordinator
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
class UpcomingViewModel internal constructor(
    private val loadUpcoming: suspend () -> List<UpcomingMonth>
) : ViewModel() {
    @Inject
    constructor(listUpcoming: ListUpcomingMonthsUseCase) : this(
        loadUpcoming = { listUpcoming() }
    )

    private val _state = MutableStateFlow(UpcomingUiState())
    val state: StateFlow<UpcomingUiState> = _state.asStateFlow()
    private val refreshCoordinator = AsyncLoadCoordinator()

    init {
        refresh()
    }

    fun refresh() {
        val loadId = refreshCoordinator.startLoad()
        viewModelScope.launch {
            try {
                val previous = _state.value
                if (previous.months.isEmpty()) {
                    _state.update { it.copy(loading = true) }
                }
                val months = loadUpcoming()
                if (refreshCoordinator.isStale(loadId)) return@launch
                _state.update { it.copy(loading = false, months = months) }
            } catch (_: Exception) {
                if (refreshCoordinator.isStale(loadId)) return@launch
                _state.update { it.copy(loading = false) }
            }
        }
    }
}
