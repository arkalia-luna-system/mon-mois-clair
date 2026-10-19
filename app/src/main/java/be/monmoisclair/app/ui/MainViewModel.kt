package be.monmoisclair.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.domain.usecase.ObserveOnboardingStateUseCase
import be.monmoisclair.app.domain.usecase.StartDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val loading: Boolean = true,
    val startDestination: StartDestination = StartDestination.SafSetup
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val observeOnboarding: ObserveOnboardingStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshStart()
    }

    fun refreshStart() {
        viewModelScope.launch {
            val dest = observeOnboarding.startDestination()
            _uiState.value = MainUiState(loading = false, startDestination = dest)
        }
    }
}
