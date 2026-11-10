package be.monmoisclair.app.ui.dossiers

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.usecase.CreatePendingLetterResult
import be.monmoisclair.app.domain.usecase.CreatePendingLetterUseCase
import be.monmoisclair.app.ui.entry.EntryFormMessages
import be.monmoisclair.app.ui.SingleFlightGate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CancellationException
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

internal const val PENDING_LETTER_SAVE_FAILED =
    "Le courrier n’a pas pu être enregistré."

internal fun pendingLetterSaveFailedState(
    state: PendingLetterFormUiState
): PendingLetterFormUiState = state.copy(
    saving = false,
    error = PENDING_LETTER_SAVE_FAILED
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
    private val saveGate = SingleFlightGate()

    fun setWho(v: String) = _state.update { it.copy(who = v, error = null) }
    fun setWhat(v: String) = _state.update { it.copy(what = v, error = null) }

    fun attachPreviewBitmap(b: Bitmap) {
        pendingBitmap = b
        _state.update { it.copy(hasPendingPhoto = true, error = null) }
    }

    fun setPhotoError() = _state.update {
        it.copy(error = EntryFormMessages.PHOTO_DECODE_ERROR)
    }

    fun setCameraError(message: String) = _state.update {
        it.copy(error = message)
    }

    fun save() {
        if (_state.value.saving || _state.value.saved) return
        if (!saveGate.tryEnter()) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update(::pendingLetterSaveFailedState)
            } finally {
                saveGate.exit()
            }
        }
    }
}
