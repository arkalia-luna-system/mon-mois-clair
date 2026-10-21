package be.monmoisclair.app.ui.dossiers

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.usecase.CreatePendingLetterResult
import be.monmoisclair.app.domain.usecase.CreatePendingLetterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingLetterFormUiState(
    val who: String = "",
    val what: String = "",
    val hasPendingPhoto: Boolean = false,
    val error: String? = null,
    val saving: Boolean = false,
    val saved: Boolean = false
)

@HiltViewModel
class PendingLetterFormViewModel @Inject constructor(
    private val createPendingLetter: CreatePendingLetterUseCase,
    private val receiptPhotos: ReceiptPhotoStore,
    private val mutatingRepository: MutatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PendingLetterFormUiState())
    val state: StateFlow<PendingLetterFormUiState> = _state.asStateFlow()

    @Volatile
    private var pendingBitmap: Bitmap? = null

    fun setWho(v: String) = _state.update { it.copy(who = v, error = null) }
    fun setWhat(v: String) = _state.update { it.copy(what = v, error = null) }

    fun attachPreviewBitmap(b: Bitmap) {
        pendingBitmap = b
        _state.update { it.copy(hasPendingPhoto = true, error = null) }
    }

    fun setPhotoError() = _state.update {
        it.copy(error = "La photo n’a pas pu être lue. Réessayez.")
    }

    fun save() {
        if (_state.value.saving || _state.value.saved) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val s = _state.value
            when (
                val result = createPendingLetter(who = s.who, what = s.what)
            ) {
                is CreatePendingLetterResult.InvalidInput ->
                    _state.update { it.copy(saving = false, error = result.message) }
                is CreatePendingLetterResult.Success -> {
                    val bmp = pendingBitmap
                    if (bmp != null) {
                        runCatching {
                            val saved = receiptPhotos.saveBitmap(result.id, bmp)
                            mutatingRepository.mutate(rescheduleReminders = false) {
                                val letter = pendingLetterDao.getById(result.id) ?: return@mutate
                                pendingLetterDao.upsert(
                                    letter.copy(receiptRelativePath = saved.relativePath)
                                )
                            }
                        }
                        pendingBitmap = null
                    }
                    _state.update { it.copy(saving = false, saved = true) }
                }
            }
        }
    }
}
