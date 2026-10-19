package be.monmoisclair.app.ui.entry

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.EpcPayment
import be.monmoisclair.app.domain.EpcQrParser
import be.monmoisclair.app.domain.RecurrenceAnchor
import be.monmoisclair.app.domain.usecase.AddEntryResult
import be.monmoisclair.app.domain.usecase.AddEntryUseCase
import be.monmoisclair.app.ui.format.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RecurrenceChoice { Once, Monthly, Every3Months, Yearly }

data class EntryFormState(
    val label: String = "",
    val amountText: String = "",
    val dayText: String = AppClock.today().dayOfMonth.toString(),
    val isExpense: Boolean = true,
    val recurrence: RecurrenceChoice = RecurrenceChoice.Once,
    val nextDueMonth: Int = AppClock.currentYearMonth().monthValue,
    val isAutoDebit: Boolean = false,
    val paymentNote: String = "",
    val hasPendingPhoto: Boolean = false,
    val copyMessage: String? = null,
    val error: String? = null,
    val saved: Boolean = false,
    val saving: Boolean = false
) {
    val needsNextDueMonth: Boolean
        get() = recurrence == RecurrenceChoice.Every3Months || recurrence == RecurrenceChoice.Yearly
}

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val addEntry: AddEntryUseCase,
    private val receiptPhotos: ReceiptPhotoStore,
    private val instanceDao: InstanceDao,
    private val mutatingRepository: MutatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(EntryFormState())
    val state: StateFlow<EntryFormState> = _state.asStateFlow()
    @Volatile private var pendingBitmap: Bitmap? = null

    fun setLabel(v: String) = _state.update { it.copy(label = v, error = null) }
    fun setAmount(v: String) = _state.update { it.copy(amountText = v, error = null) }
    fun setDay(v: String) = _state.update { it.copy(dayText = v, error = null) }
    fun setExpense(v: Boolean) = _state.update {
        it.copy(isExpense = v, isAutoDebit = if (v) it.isAutoDebit else false)
    }
    fun setRecurrence(v: RecurrenceChoice) = _state.update { it.copy(recurrence = v) }
    fun setNextDueMonth(m: Int) = _state.update { it.copy(nextDueMonth = m.coerceIn(1, 12)) }
    fun setAutoDebit(v: Boolean) = _state.update { it.copy(isAutoDebit = v) }
    fun setPaymentNote(v: String) = _state.update {
        it.copy(paymentNote = v, error = null, copyMessage = null)
    }
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

    fun applyEpcRaw(raw: String) {
        val p = EpcQrParser.parse(raw)
        if (p == null) {
            _state.update { it.copy(error = "QR Code non reconnu (facture belge EPC attendue).") }
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
                isExpense = true,
                label = payment.beneficiaryName.ifBlank { it.label },
                amountText = amountText,
                paymentNote = payment.remittance.ifBlank { it.paymentNote },
                error = null
            )
        }
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
            val recurring = s.recurrence != RecurrenceChoice.Once
            val interval = when (s.recurrence) {
                RecurrenceChoice.Once, RecurrenceChoice.Monthly -> 1
                RecurrenceChoice.Every3Months -> 3
                RecurrenceChoice.Yearly -> 12
            }
            val anchor = when {
                !recurring -> null
                s.needsNextDueMonth -> RecurrenceAnchor.nextYearMonth(s.nextDueMonth)
                else -> AppClock.currentYearMonth()
            }
            _state.update { it.copy(saving = true, error = null) }
            when (
                val result = addEntry(
                    label = s.label, amountCents = cents, dayOfMonth = day,
                    isExpense = s.isExpense, recurring = recurring,
                    intervalMonths = interval, anchorYearMonth = anchor,
                    isAutoDebit = s.isExpense && s.isAutoDebit, paymentNote = s.paymentNote
                )
            ) {
                is AddEntryResult.Success -> {
                    pendingBitmap?.let { bmp ->
                        runCatching {
                            val path = receiptPhotos.saveBitmap(result.instanceId, bmp)
                            mutatingRepository.mutate {
                                instanceDao.getExpenseById(result.instanceId)?.let {
                                    instanceDao.updateExpense(it.copy(receiptRelativePath = path))
                                }
                            }
                        }
                    }
                    pendingBitmap = null
                    _state.update { it.copy(saving = false, saved = true) }
                }
                is AddEntryResult.InvalidInput ->
                    _state.update { it.copy(saving = false, error = result.message) }
            }
        }
    }
}
