package be.monmoisclair.app.ui.entry

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.domain.CreditorMailComposer
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.EpcPayment
import be.monmoisclair.app.domain.EpcQrParser
import be.monmoisclair.app.domain.ReceiptPathList
import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.domain.usecase.DeleteInstanceResult
import be.monmoisclair.app.domain.usecase.DeleteInstanceUseCase
import be.monmoisclair.app.domain.usecase.UpdateInstanceResult
import be.monmoisclair.app.domain.usecase.UpdateInstanceUseCase
import be.monmoisclair.app.ui.clipboard.SensitiveClipboard
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
    /** L’utilisateur a touché « Enlever la photo ». */
    val receiptCleared: Boolean = false,
    val hasPendingPhoto: Boolean = false,
    val copyMessage: String? = null,
    val recurrenceInfo: String? = null,
    val recurrence: RecurrenceChoice = RecurrenceChoice.Once,
    val hasTemplate: Boolean = false,
    val applyToFollowing: Boolean = false,
    val dossierExpanded: Boolean = false,
    val causeLabel: String = "",
    /** Saisie seulement pour convertir une facture en dossier. */
    val openingRemainingText: String = "",
    /** Déjà un dossier (opening fixé). */
    val isAgreement: Boolean = false,
    /** Reste calculé (null = pas un dossier). */
    val remainingCents: Long? = null,
    val isSettled: Boolean = false,
    /** Lot C — visibles seulement sur un dossier. */
    val iban: String = "",
    val creditorEmail: String = "",
    val addedFeesCents: Long = 0L,
    val addedFeesNote: String = "",
    val feeAddText: String = "",
    val feeNoteText: String = "",
    val extraReceiptPaths: String = "",
    val error: String? = null,
    val done: Boolean = false,
    val loading: Boolean = true,
    /** Intent mail à lancer une fois (consommé par l’UI). */
    val mailDraft: CreditorMailComposer.Draft? = null
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
            _state.update {
                it.copy(
                    loading = false, label = e.label,
                    amountText = MoneyFormat.centsToEditable(e.amountCents),
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
        val tpl = e.templateId?.let { templateDao.getExpenseById(it) }
        val opening = tpl?.openingRemainingCents
        val paid = e.templateId?.let {
            instanceDao.sumAmountCentsByTemplateAndStatus(it, ExpenseStatus.DONE)
        } ?: 0L
        val fees = tpl?.addedFeesCents ?: 0L
        val remaining = DossierRemaining.remainingCents(opening, paid, fees)
        _state.update {
            it.copy(
                loading = false, label = e.label,
                amountText = MoneyFormat.centsToEditable(e.amountCents),
                dayText = e.dayOfMonth.toString(), isAutoDebit = e.isAutoDebit,
                paymentNote = e.paymentNote, receiptRelativePath = e.receiptRelativePath,
                recurrenceInfo = recurrenceInfo, recurrence = choice,
                hasTemplate = e.templateId != null,
                causeLabel = tpl?.causeLabel.orEmpty(),
                isAgreement = DossierRemaining.isDossier(opening),
                remainingCents = remaining,
                isSettled = DossierRemaining.isSettled(opening, paid, fees),
                dossierExpanded = DossierRemaining.isDossier(opening),
                iban = tpl?.iban.orEmpty(),
                creditorEmail = tpl?.creditorEmail.orEmpty(),
                addedFeesCents = fees,
                addedFeesNote = tpl?.addedFeesNote.orEmpty(),
                extraReceiptPaths = tpl?.extraReceiptPaths.orEmpty()
            )
        }
    }

    fun setLabel(v: String) = _state.update { it.copy(label = v, error = null) }
    fun setAmount(v: String) = _state.update { it.copy(amountText = v, error = null) }
    fun setDay(v: String) = _state.update { it.copy(dayText = v, error = null) }
    fun setAutoDebit(v: Boolean) = _state.update { it.copy(isAutoDebit = v) }
    fun setPaymentNote(v: String) = _state.update { it.copy(paymentNote = v) }
    fun setApplyToFollowing(v: Boolean) = _state.update { it.copy(applyToFollowing = v) }
    /**
     * Collapse sur une conversion (pas encore dossier) = annuler : wipe cause / reste.
     * Sur un accord existant : on replie seulement (pas d’effacement → pas de sauvegarde vide).
     */
    fun setDossierExpanded(v: Boolean) = _state.update {
        when {
            !v && !it.isAgreement -> it.copy(
                dossierExpanded = false,
                causeLabel = "",
                openingRemainingText = "",
                error = null
            )
            else -> it.copy(dossierExpanded = v, error = null)
        }
    }
    fun setCauseLabel(v: String) = _state.update { it.copy(causeLabel = v, error = null) }
    fun setOpeningRemainingText(v: String) = _state.update {
        it.copy(openingRemainingText = v, error = null)
    }
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
    fun attachPreviewBitmap(b: Bitmap) {
        pendingBitmap = b
        _state.update { it.copy(hasPendingPhoto = true, receiptCleared = false, error = null) }
    }

    fun setPhotoDecodeError() = _state.update {
        it.copy(error = "La photo n’a pas pu être lue. Réessayez.")
    }
    fun clearReceiptPhoto() {
        pendingBitmap = null
        _state.update {
            it.copy(
                hasPendingPhoto = false,
                receiptRelativePath = "",
                receiptCleared = true,
                error = null
            )
        }
    }

    /** Retire un courrier en plus (persisté à l’enregistrement). */
    fun removeExtraReceipt(path: String) {
        val target = path.trim()
        if (target.isEmpty()) return
        _state.update {
            it.copy(
                extraReceiptPaths = ReceiptPathList.remove(it.extraReceiptPaths, target),
                error = null
            )
        }
    }

    suspend fun openReceiptUri() = receiptPhotos.openUri(_state.value.receiptRelativePath)
    suspend fun decodeReceiptBitmap(relativePath: String = _state.value.receiptRelativePath) =
        receiptPhotos.decodeBitmap(relativePath)

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
    fun setFeeAddText(v: String) = _state.update { it.copy(feeAddText = v, error = null) }
    fun setFeeNoteText(v: String) = _state.update { it.copy(feeNoteText = v, error = null) }

    fun copyIban() {
        val iban = _state.value.iban.trim()
        if (iban.isEmpty()) {
            _state.update { it.copy(error = "Aucun IBAN à copier.", copyMessage = null) }
            return
        }
        SensitiveClipboard.setPlainText(appContext, "iban", iban)
        _state.update {
            it.copy(
                copyMessage = BelgianStructuredCommunication.IBAN_COPIED_OK,
                error = null
            )
        }
    }

    fun prepareMail() {
        val s = _state.value
        val cents = MoneyFormat.parseToCents(s.amountText) ?: 0L
        val draft = CreditorMailComposer.build(
            email = s.creditorEmail,
            who = s.label,
            what = s.causeLabel,
            monthAmountCents = cents,
            paymentNote = s.paymentNote
        )
        if (draft.to.isBlank()) {
            _state.update { it.copy(error = "Indiquez d’abord l’e-mail du créancier.") }
            return
        }
        _state.update { it.copy(mailDraft = draft, error = null) }
    }

    fun consumeMailDraft() = _state.update { it.copy(mailDraft = null) }

    fun mailLaunchFailed() = _state.update {
        it.copy(
            mailDraft = null,
            error = "Aucune application Mail trouvée sur cet appareil."
        )
    }

    fun copyPaymentNote() {
        val prepared = BelgianStructuredCommunication.prepareCopy(_state.value.paymentNote)
        if (prepared.clipboardText.isEmpty()) {
            _state.update {
                it.copy(error = prepared.userMessage, copyMessage = null)
            }
            return
        }
        SensitiveClipboard.setPlainText(appContext, "communication", prepared.clipboardText)
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
            var extraAppend = ""
            val cleared = s.receiptCleared && pendingBitmap == null
            pendingBitmap?.let { bmp ->
                val owner = if (s.applyToFollowing) {
                    instanceDao.getExpenseById(s.id)?.templateId ?: s.id
                } else {
                    s.id
                }
                if (s.isAgreement && path.isNotBlank() && !cleared) {
                    extraAppend = receiptPhotos.saveBitmap(
                        "$owner-extra-${AppClock.nowEpochMs()}",
                        bmp
                    )
                } else {
                    path = receiptPhotos.saveBitmap(owner, bmp)
                }
                pendingBitmap = null
            }
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
            val wantOpening = s.isExpense && s.dossierExpanded && !s.isAgreement
            val openingRaw = s.openingRemainingText.trim()
            val openingCents = when {
                !wantOpening || openingRaw.isEmpty() -> null
                else -> MoneyFormat.parseToCents(openingRaw)
            }
            if (wantOpening && openingRaw.isNotEmpty() && openingCents == null) {
                _state.update { it.copy(error = "Vérifiez le reste dû (montant).") }
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
            val feeRaw = s.feeAddText.trim()
            val feeAdd = when {
                !s.isAgreement || feeRaw.isEmpty() -> 0L
                else -> MoneyFormat.parseToCents(feeRaw)
            }
            if (s.isAgreement && feeRaw.isNotEmpty() && feeAdd == null) {
                _state.update { it.copy(error = "Vérifiez le montant des frais.") }
                return@launch
            }
            val syncCreditor = s.isExpense && (s.isAgreement || s.dossierExpanded)
            when (
                val result = updateInstance(
                    id = s.id,
                    isExpense = s.isExpense,
                    label = s.label,
                    amountCents = cents,
                    dayOfMonth = day,
                    applyToFollowingMonths = s.applyToFollowing,
                    isAutoDebit = s.isExpense && s.isAutoDebit,
                    paymentNote = s.paymentNote,
                    receiptRelativePath = path,
                    intervalMonths = interval,
                    clearReceipt = cleared && path.isBlank(),
                    causeLabel = when {
                        !s.isExpense -> ""
                        s.isAgreement || s.dossierExpanded -> s.causeLabel
                        else -> ""
                    },
                    openingRemainingCents = openingCents,
                    setOpeningRemaining = openingCents != null,
                    iban = if (syncCreditor) s.iban else "",
                    creditorEmail = if (syncCreditor) s.creditorEmail else "",
                    syncCreditorDetails = syncCreditor,
                    feeAddCents = feeAdd ?: 0L,
                    feeNote = s.feeNoteText,
                    extraReceiptAppend = extraAppend,
                    extraReceiptPathsReplace = if (s.isAgreement) s.extraReceiptPaths else null
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
