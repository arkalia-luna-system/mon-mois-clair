package be.monmoisclair.app.ui.dossiers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.entity.PendingLetterEntity
import be.monmoisclair.app.domain.usecase.DossiersSnapshot
import be.monmoisclair.app.domain.usecase.ListDossiersUseCase
import be.monmoisclair.app.domain.usecase.ListPendingLettersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DossiersUiState(
    val loading: Boolean = true,
    val snapshot: DossiersSnapshot = DossiersSnapshot(emptyList(), 0L),
    val pendingLetters: List<PendingLetterEntity> = emptyList()
)

@HiltViewModel
class DossiersViewModel @Inject constructor(
    private val listDossiers: ListDossiersUseCase,
    private val listPendingLetters: ListPendingLettersUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(DossiersUiState())
    val state: StateFlow<DossiersUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = DossiersUiState(loading = true)
            val snap = listDossiers()
            val pending = listPendingLetters()
            _state.value = DossiersUiState(
                loading = false,
                snapshot = snap,
                pendingLetters = pending
            )
        }
    }
}
