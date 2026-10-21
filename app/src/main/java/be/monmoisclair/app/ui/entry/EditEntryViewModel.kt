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
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.domain.EpcPayment
import be.monmoisclair.app.domain.EpcQrParser
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
    val recurrence: RecurrenceChoice = RecurrenceChoice.Once,
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
                _state.update { it.copy(loading = false, error = "Introuvable.") }; return
            }
            val rem = (e.amountCents % 100).toInt()
            _state.update {
                it.copy(
                    loading = false, label = e.label,
                    amountText = if (rem == 0) (e.amountCents / 100).toString() else "%d,%02d".format(e.amountCents / 100, rem),
                    dayText = e.dayOfMonth.toString(),
                    recurrenceInfo = if (e.templateId != null) RecurrenceLabel.forIntervalMonths(1) else RecurrenceLabel.oneOff(),
                    recurrence = if (e.templateId != null) RecurrenceChoice.Monthly else RecurrenceChoice.Once,
                    hasTemplate = e.templateId != null
                )
            }
            return
        }
        val e = instanceDao.getExpenseById(entryId) ?: run {
            _state.update { it.copy(loading = false, error = "Introuvable.") }; return
        }
        val rem = (e.amountCents % 100).toInt()
        val interval = e.templateId?.let { tid ->
            templateDao.getExpenseById(tid)?.intervalMonths
        }
        val choice = when (interval) {
            null -> RecurrenceChoice.Once
            3 -> RecurrenceChoice.Every3Months
            12 -> RecurrenceChoice.Yearly
            else -> RecurrenceChoice.Monthly
        }
        val recurrenceInfo = interval?.let { RecurrenceLabel.forIntervalMonths(it) }
            ?: RecurrenceLabel.oneOff()
        _state.update {
            it.copy(
                loading = false, label = e.label,
                amountText = if (rem == 0) (e.amountCents / 100).toString() else "%d,%02d".format(e.amountCents / 100, rem),
                dayText = e.dayOfMonth.toString(), isAutoDebit = e.isAutoDebit,
                paymentNote = e.paymentNote, receiptRelativePath = e.receiptRelativePath,
                recurrenceInfo = recurrenceInfo, recurrence = choice,
                hasTemplate = e.templateId != null
            )
        }
    }

    fun setLabel(v: String) = _state.update { it.copy(label = v, error = null) }
    fun setAmount(v: String) = _state.update { it.copy(amountText = v, error = null) }
    fun setDay(v: String) = _state.update { it.copy(dayText = v, error = null) }
    fun setAutoDebit(v: Boolean) = _state.update { it.copy(isAutoDebit = v) }
    fun setPaymentNote(v: String) = _state.update { it.copy(paymentNote = v) }
    fun setApplyToFollowing(v: Boolean) = _state.update { it.copy(applyToFollowing = v) }
    fun setRecurrence(v: RecurrenceChoice) = _state.update {
        it.copy(
            recurrence = v,
            recurrenceInfo = when (v) {
                RecurrenceChoice.Once -> RecurrenceLabel.oneOff()
                RecurrenceChoice.Monthly -> RecurrenceLabel.forIntervalMonths(1)
                RecurrenceChoice.Every3Months -> RecurrenceLabel.forIntervalMonths(3)
                RecurrenceChoice.Yearly -> RecurrenceLabel.forIntervalMonths(12)
            },
            applyToFollowing = if (v != RecurrenceChoice.Once && it.hasTemplate) true else it.applyToFollowing,
            error = null
        )
    }
    fun attachPreviewBitmap(b: Bitmap) { pendingBitmap = b; _state.update { it.copy(hasPendingPhoto = true) } }
    fun clearReceiptPhoto() {
        pendingBitmap = null
        _state.update {
            it.copy(hasPendingPhoto = false, receiptRelativePath = "", error = null)
        }
    }
    suspend fun openReceiptUri() = receiptPhotos.openUri(_state.value.receiptRelativePath)
    suspend fun decodeReceiptBitmap() =
        receiptPhotos.decodeBitmap(_state.value.receiptRelativePath)

    fun applyEpcRaw(raw: String) {
        val p = EpcQrParser.parse(raw)
        if (p == null) {
            _state.update { it.copy(error = "Ce QR n’est pas une facture belge.") }
            return
        }
        applyEpc(p)
    }

    fun applyEpc(payment: EpcPayment) {
        val amountText = payment.amountCents?.let { c ->
            val rem = (c % 100).toInt()
            if (rem == 0) (c / 100).toString() else "%d,%02d".format(c / 100, rem)
        } ?: _state.value.amountText
        _state.update {
            it.copy(
                label = payment.beneficiaryName.ifBlank { it.label },
                amountText = amountText,
                paymentNote = payment.remittance.ifBlank { it.paymentNote },
                error = null
            )
        }
    }

    fun copyPaymentNote() {
        val prepared = BelgianStructuredCommunication.prepareCopy(_state.value.paymentNote)
        if (prepared.clipboardText.isEmpty()) {
            _state.update {
                it.copy(error = prepared.userMessage, copyMessage = null)
            }
            return
        }
        (appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText("communication", prepared.clipboardText))
        _state.update {
            it.copy(copyMessage = prepared.userMessage, error = null)
        }
    }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            if (MoneyFormat.tooManyFractionDigits(s.amountText)) {
                _state.update { it.copy(error = MoneyFormat.FRACTION_HINT) }
                return@launch
            }
            val cents = MoneyFormat.parseToCents(s.amountText)
            val day = s.dayText.trim().toIntOrNull()
            if (cents == null || day == null) {
                _state.update { it.copy(error = "Vérifiez le montant et le jour (1 à 31).") }
                return@launch
            }
            var path = s.receiptRelativePath
            pendingBitmap?.let { path = receiptPhotos.saveBitmap(s.id, it); pendingBitmap = null }
            // null = inchangé ; -1 = une fois ; 1/3/12 = intervalle (crée le modèle si besoin).
            val interval: Int? = if (!s.isExpense) {
                null
            } else {
                when (s.recurrence) {
                    RecurrenceChoice.Once -> if (s.hasTemplate) -1 else null
                    RecurrenceChoice.Monthly -> 1
                    RecurrenceChoice.Every3Months -> 3
                    RecurrenceChoice.Yearly -> 12
                }
            }
            when (
                val result = updateInstance(
                    s.id, s.isExpense, s.label, cents, day, s.applyToFollowing,
                    s.isExpense && s.isAutoDebit, s.paymentNote, path, interval
                )
            ) {
                UpdateInstanceResult.Success -> _state.update { it.copy(done = true) }
                UpdateInstanceResult.NotFound -> _state.update { it.copy(error = "Introuvable.") }
                is UpdateInstanceResult.InvalidInput -> _state.update { it.copy(error = result.message) }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            when (deleteInstance(_state.value.id, _state.value.isExpense, _state.value.applyToFollowing)) {
                DeleteInstanceResult.Success -> _state.update { it.copy(done = true) }
                DeleteInstanceResult.NotFound -> _state.update { it.copy(error = "Introuvable.") }
            }
        }
    }
}
