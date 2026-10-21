package be.monmoisclair.app.ui.entry

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.domain.EpcPayment
import be.monmoisclair.app.domain.EpcQrParser
import be.monmoisclair.app.domain.RecurrenceAnchor
import be.monmoisclair.app.domain.usecase.AddEntryResult
import be.monmoisclair.app.domain.usecase.AddEntryUseCase
import be.monmoisclair.app.ui.format.DayLabelFormat
import be.monmoisclair.app.ui.format.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
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
    /** Lot B — bloc replié « retard ou accord ». */
    val dossierExpanded: Boolean = false,
    val causeLabel: String = "",
    val openingRemainingText: String = "",
    /** Lot C — IBAN du QR / saisie (dossier). */
    val iban: String = "",
    val creditorEmail: String = "",
    val copyMessage: String? = null,
    val error: String? = null,
    val saved: Boolean = false,
    val saving: Boolean = false,
    val formTitle: String = "Nouvelle facture ou revenu",
    val targetYearMonth: YearMonth = AppClock.currentYearMonth()
) {
    val needsNextDueMonth: Boolean
        get() = recurrence == RecurrenceChoice.Every3Months || recurrence == RecurrenceChoice.Yearly
}

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContext: Context,
    private val addEntry: AddEntryUseCase,
    private val receiptPhotos: ReceiptPhotoStore,
    private val instanceDao: InstanceDao,
    private val mutatingRepository: MutatingRepository
) : ViewModel() {
    private val targetYm: YearMonth = run {
        val raw = savedStateHandle.get<String>("ym")
        val parsed = runCatching { YearMonth.parse(raw) }.getOrNull()
        val current = AppClock.currentYearMonth()
        when {
            parsed == null -> current
            parsed.isAfter(current) -> current
            else -> parsed
        }
    }
    private val _state = MutableStateFlow(
        EntryFormState(
            targetYearMonth = targetYm,
            formTitle = if (targetYm == AppClock.currentYearMonth()) {
                "Nouvelle facture ou revenu"
            } else {
                "Nouvelle facture ou revenu · ${DayLabelFormat.monthTitle(targetYm)}"
            },
            nextDueMonth = targetYm.monthValue
        )
    )
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
    /** Collapse = annuler : on efface cause / reste pour ne pas créer un dossier par erreur. */
    fun setDossierExpanded(v: Boolean) = _state.update {
        if (!v) {
            it.copy(
                dossierExpanded = false,
                causeLabel = "",
                openingRemainingText = "",
                error = null
            )
        } else {
            it.copy(dossierExpanded = true, error = null)
        }
    }
    fun setCauseLabel(v: String) = _state.update { it.copy(causeLabel = v, error = null) }
    fun setOpeningRemainingText(v: String) = _state.update {
        it.copy(openingRemainingText = v, error = null)
    }
    fun attachPreviewBitmap(b: Bitmap) { pendingBitmap = b; _state.update { it.copy(hasPendingPhoto = true) } }

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

    fun applyEpcRaw(raw: String) {
        val p = EpcQrParser.parse(raw)
        if (p == null) {
            _state.update { it.copy(error = "Ce QR n’est pas une facture belge.") }
            return
        }
        applyEpc(p)
    }

    fun applyEpc(payment: EpcPayment) {
        val amountText = payment.amountCents?.let { MoneyFormat.centsToEditable(it) }
            ?: _state.value.amountText
        _state.update {
            it.copy(
                isExpense = true,
                label = payment.beneficiaryName.ifBlank { it.label },
                amountText = amountText,
                paymentNote = payment.remittance.ifBlank { it.paymentNote },
                iban = payment.iban.ifBlank { it.iban },
                error = null
            )
        }
    }

    fun setIban(v: String) = _state.update { it.copy(iban = v, error = null) }
    fun setCreditorEmail(v: String) = _state.update { it.copy(creditorEmail = v, error = null) }

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
            val openingRaw = s.openingRemainingText.trim()
            val openingCents = when {
                !s.isExpense || !s.dossierExpanded || openingRaw.isEmpty() -> null
                else -> MoneyFormat.parseToCents(openingRaw)
            }
            if (s.isExpense && s.dossierExpanded && openingRaw.isNotEmpty() && openingCents == null) {
                _state.update { it.copy(error = "Vérifiez le reste dû (montant).") }
                return@launch
            }
            // IBAN / e-mail (souvent via QR) ne doivent pas disparaître en silence
            // si le reste dû est encore vide — sinon on croit avoir créé un dossier.
            val hasCreditorBits = s.iban.isNotBlank() || s.creditorEmail.isNotBlank()
            if (s.isExpense && s.dossierExpanded && openingCents == null && hasCreditorBits) {
                _state.update {
                    it.copy(
                        error = "Indiquez le reste dû, ou videz l’IBAN et l’e-mail."
                    )
                }
                return@launch
            }
            if (openingCents != null && s.recurrence == RecurrenceChoice.Once) {
                _state.update {
                    it.copy(
                        error = "Un accord doit être une facture qui revient " +
                            "(mensuel, 3 mois ou annuel)."
                    )
                }
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
                s.needsNextDueMonth ->
                    RecurrenceAnchor.nextYearMonth(s.nextDueMonth, from = s.targetYearMonth)
                else -> s.targetYearMonth
            }
            _state.update { it.copy(saving = true, error = null) }
            when (
                val result = addEntry(
                    label = s.label, amountCents = cents, dayOfMonth = day,
                    isExpense = s.isExpense, recurring = recurring,
                    intervalMonths = interval,
                    targetYearMonth = s.targetYearMonth,
                    anchorYearMonth = anchor,
                    isAutoDebit = s.isExpense && s.isAutoDebit,
                    paymentNote = s.paymentNote,
                    causeLabel = if (s.isExpense && s.dossierExpanded) s.causeLabel else "",
                    openingRemainingCents = openingCents,
                    iban = if (openingCents != null) s.iban else "",
                    creditorEmail = if (openingCents != null) s.creditorEmail else ""
                )
            ) {
                is AddEntryResult.Success -> {
                    val photoFailed = pendingBitmap?.let { bmp ->
                        runCatching {
                            val expense = instanceDao.getExpenseById(result.instanceId)
                            if (expense != null) {
                                val instancePath = receiptPhotos.saveBitmap(expense.id, bmp)
                                val templateId = expense.templateId
                                val canonical = templateId?.let { receiptPhotos.saveBitmap(it, bmp) }
                                mutatingRepository.mutate {
                                    instanceDao.getExpenseById(expense.id)?.let {
                                        instanceDao.updateExpense(
                                            it.copy(receiptRelativePath = instancePath)
                                        )
                                    }
                                    if (templateId != null && canonical != null) {
                                        templateDao.getExpenseById(templateId)?.let { tpl ->
                                            templateDao.updateExpense(
                                                tpl.copy(receiptRelativePath = canonical)
                                            )
                                        }
                                    }
                                }
                            } else {
                                val canonical = receiptPhotos.saveBitmap(result.instanceId, bmp)
                                mutatingRepository.mutate {
                                    templateDao.getExpenseById(result.instanceId)?.let { tpl ->
                                        templateDao.updateExpense(
                                            tpl.copy(receiptRelativePath = canonical)
                                        )
                                    }
                                }
                            }
                        }.isFailure
                    } == true
                    pendingBitmap = null
                    _state.update {
                        it.copy(
                            saving = false,
                            saved = !photoFailed,
                            error = if (photoFailed) {
                                "La facture est notée, mais la photo n’a pas pu être enregistrée."
                            } else {
                                null
                            }
                        )
                    }
                }
                is AddEntryResult.InvalidInput ->
                    _state.update { it.copy(saving = false, error = result.message) }
            }
        }
    }
}
