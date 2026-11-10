package be.monmoisclair.app.ui.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.backup.SafBackupStore
import be.monmoisclair.app.domain.usecase.PersistSafFolderUseCase
import be.monmoisclair.app.ui.SingleFlightGate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SafSetupUiState(
    val saving: Boolean = false,
    val errorMessage: String? = null,
    val completed: Boolean = false
)

@HiltViewModel
class SafSetupViewModel @Inject constructor(
    private val safBackupStore: SafBackupStore,
    private val persistSafFolder: PersistSafFolderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SafSetupUiState())
    val uiState: StateFlow<SafSetupUiState> = _uiState.asStateFlow()
    private val pickGate = SingleFlightGate()

    fun openDocumentTreeIntent() = safBackupStore.buildOpenDocumentTreeIntent()

    fun onFolderPicked(uri: Uri?) {
        if (uri == null) {
            _uiState.update {
                it.copy(errorMessage = "Aucun dossier choisi. C’est nécessaire pour la sauvegarde.")
            }
            return
        }
        if (!pickGate.tryEnter()) return
        viewModelScope.launch {
            try {
            _uiState.update { it.copy(saving = true, errorMessage = null) }
            when (val result = persistSafFolder(uri)) {
                PersistSafFolderUseCase.Result.Success ->
                    _uiState.update { it.copy(saving = false, completed = true) }
                is PersistSafFolderUseCase.Result.Failed ->
                    _uiState.update {
                        it.copy(saving = false, errorMessage = result.message)
                    }
            }
            } finally {
                pickGate.exit()
            }
        }
    }
}
