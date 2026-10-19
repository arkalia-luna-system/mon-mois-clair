package be.monmoisclair.app.ui.entry

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.domain.usecase.DeleteInstanceResult
import be.monmoisclair.app.domain.usecase.DeleteInstanceUseCase
import be.monmoisclair.app.domain.usecase.UpdateInstanceResult
import be.monmoisclair.app.domain.usecase.UpdateInstanceUseCase
import be.monmoisclair.app.ui.format.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditEntryState(
    val id: String = "",
    val isExpense: Boolean = true,
    val label: String = "",
    val amountText: String = "",
    val dayText: String = "1",
    val isAutoDebit: Boolean = false,
    val paymentNote: String = "",
    val receiptRelativePath: String = "",
    val hasPendingPhoto: Boolean = false,
    val copyMessage: String? = null,
    val recurrenceInfo: String? = null,
    val hasTemplate: Boolean = false,
    val applyToFollowing: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
    val loading: Boolean = true
)

@HiltViewModel
class EditEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContext: Context,
    private val instanceDao: InstanceDao,
    private val templateDao: TemplateDao,
    private val updateInstance: UpdateInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val receiptPhotos: ReceiptPhotoStore
) : ViewModel() {
    private val entryId: String = checkNotNull(savedStateHandle["id"])
    private val isExpenseArg: Boolean = savedStateHandle.get<String>("isExpense") == "true"
    private val _state = MutableStateFlow(EditEntryState(id = entryId, isExpense = isExpenseArg))
    val state: StateFlow<EditEntryState> = _state.asStateFlow()
    @Volatile private var pendingBitmap: Bitmap? = null

    init { viewModelScope.launch { load() } }

    private suspend fun load() {
        if (!isExpenseArg) {
            val e = instanceDao.getIncomeById(entryId) ?: run {
                _state.update { it.copy(loading = false, error = "Entrée introuvable.") }; return
            }
            val rem = (e.amountCents % 100).toInt()
            _state.update {
                it.copy(
                    loading = false, label = e.label,
                    amountText = if (rem == 0) (e.amountCents / 100).toString() else "%d,%02d".format(e.amountCents / 100, rem),
                    dayText = e.dayOfMonth.toString(),
                    recurrenceInfo = if (e.templateId != null) RecurrenceLabel.forIntervalMonths(1) else RecurrenceLabel.oneOff(),
                    hasTemplate = e.templateId != null
                )
            }
            return
        }
        val e = instanceDao.getExpenseById(entryId) ?: run {
            _state.update { it.copy(loading = false, error = "Entrée introuvable.") }; return
        }
        val rem = (e.amountCents % 100).toInt()
        val recurrenceInfo = e.templateId?.let { tid ->
            templateDao.getExpenseById(tid)?.let { RecurrenceLabel.forIntervalMonths(it.intervalMonths) }
        } ?: RecurrenceLabel.oneOff()
        _state.update {
            it.copy(
                loading = false, label = e.label,
                amountText = if (rem == 0) (e.amountCents / 100).toString() else "%d,%02d".format(e.amountCents / 100, rem),
                dayText = e.dayOfMonth.toString(), isAutoDebit = e.isAutoDebit,
                paymentNote = e.paymentNote, receiptRelativePath = e.receiptRelativePath,
                recurrenceInfo = recurrenceInfo, hasTemplate = e.templateId != null
            )
        }
    }

    fun setLabel(v: String) = _state.update { it.copy(label = v, error = null) }
    fun setAmount(v: String) = _state.update { it.copy(amountText = v, error = null) }
    fun setDay(v: String) = _state.update { it.copy(dayText = v, error = null) }
    fun setAutoDebit(v: Boolean) = _state.update { it.copy(isAutoDebit = v) }
    fun setPaymentNote(v: String) = _state.update { it.copy(paymentNote = v) }
    fun setApplyToFollowing(v: Boolean) = _state.update { it.copy(applyToFollowing = v) }
    fun attachPreviewBitmap(b: Bitmap) { pendingBitmap = b; _state.update { it.copy(hasPendingPhoto = true) } }

    fun copyPaymentNote() {
        val note = _state.value.paymentNote.trim()
        if (note.isEmpty()) {
            _state.update { it.copy(copyMessage = "Aucune communication à copier.") }
            return
        }
        (appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText("communication", note))
        _state.update { it.copy(copyMessage = "Copié — collez dans votre application bancaire.") }
    }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            val cents = MoneyFormat.parseToCents(s.amountText)
            val day = s.dayText.trim().toIntOrNull()
            if (cents == null || day == null) {
                _state.update { it.copy(error = "Vérifiez le montant et le jour (1 à 31).") }
                return@launch
            }
            var path = s.receiptRelativePath
            pendingBitmap?.let { path = receiptPhotos.saveBitmap(s.id, it); pendingBitmap = null }
            when (
                updateInstance(
                    s.id, s.isExpense, s.label, cents, day, s.applyToFollowing,
                    s.isExpense && s.isAutoDebit, s.paymentNote, path
                )
            ) {
                UpdateInstanceResult.Success -> _state.update { it.copy(done = true) }
                UpdateInstanceResult.NotFound -> _state.update { it.copy(error = "Entrée introuvable.") }
                is UpdateInstanceResult.InvalidInput -> _state.update { it.copy(error = result.message) }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            when (deleteInstance(_state.value.id, _state.value.isExpense, _state.value.applyToFollowing)) {
                DeleteInstanceResult.Success -> _state.update { it.copy(done = true) }
                DeleteInstanceResult.NotFound -> _state.update { it.copy(error = "Entrée introuvable.") }
            }
        }
    }
}
