package be.monmoisclair.app.ui.restore

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.backup.BackupRestorer
import be.monmoisclair.app.data.backup.RestoreResult
import be.monmoisclair.app.domain.MonthCycleEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RestoreUiState(
    val busy: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val success: Boolean = false
)

@HiltViewModel
class RestoreBackupViewModel @Inject constructor(
    private val restorer: BackupRestorer,
    private val monthCycleEngine: MonthCycleEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestoreUiState())
    val uiState: StateFlow<RestoreUiState> = _uiState.asStateFlow()

    fun restoreFromLatest() {
        runRestore { restorer.restoreFromLatest() }
    }

    fun restoreFromUri(uri: Uri?) {
        if (uri == null) {
            _uiState.update {
                it.copy(
                    message = "Aucun fichier sélectionné.",
                    isError = true,
                    success = false
                )
            }
            return
        }
        runRestore { restorer.restoreFromUri(uri) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, isError = false) }
    }

    private fun runRestore(block: suspend () -> RestoreResult) {
        if (_uiState.value.busy) return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, message = null, isError = false, success = false) }
            val result = try {
                block()
            } catch (e: Exception) {
                RestoreResult.IoError(e.message ?: e.javaClass.simpleName)
            }
            val ok = result is RestoreResult.Success
            if (ok) {
                // Backup sans mois courant → créer le ledger lazy (charte cycle mensuel).
                monthCycleEngine.ensureCurrentMonthExists()
            }
            _uiState.update {
                it.copy(
                    busy = false,
                    message = RestoreMessages.from(result),
                    isError = !ok,
                    success = ok
                )
            }
        }
    }
}
