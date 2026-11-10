package be.monmoisclair.app.ui.entry

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.backup.BackupFailure
import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.dao.ExpenseFeeEventDao
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.data.receipt.ReceiptCommunicationOcr
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.domain.CreditorMailComposer
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.EpcPayment
import be.monmoisclair.app.domain.ReceiptPathList
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.domain.usecase.ConfigureSavingsResult
import be.monmoisclair.app.domain.usecase.ConfigureSavingsUseCase
import be.monmoisclair.app.domain.usecase.DeleteInstanceResult
import be.monmoisclair.app.domain.usecase.DeleteInstanceUseCase
import be.monmoisclair.app.domain.usecase.UpdateInstanceResult
import be.monmoisclair.app.domain.usecase.UpdateInstanceUseCase
import be.monmoisclair.app.ui.clipboard.SensitiveClipboard
import be.monmoisclair.app.ui.SingleFlightGate
import be.monmoisclair.app.ui.format.FeeEventDateFormat
import be.monmoisclair.app.ui.format.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Ligne d’historique frais pour la fiche (pas d’Android dans le domaine). */
data class FeeEventRow(
    val id: String,
    val amountCents: Long,
    val note: String,
    val dateLabel: String
)

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
    /** PDF en attente (courrier mail) — pas d’aperçu bitmap. */
    val hasPendingPdf: Boolean = false,
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
    /**
     * « Environ N mensualités… Dernière estimée » — null si soldé / non estimable,
     * ou si ce n’est pas un accord. Jamais affiché sur l’accueil.
     */
    val pacePhrase: String? = null,
    /** Lot C — visibles seulement sur un dossier. */
    val iban: String = "",
    val creditorEmail: String = "",
    val addedFeesCents: Long = 0L,
    val addedFeesNote: String = "",
    /** Historique daté (schéma 8), plus récent en premier. */
    val feeEvents: List<FeeEventRow> = emptyList(),
    val feeAddText: String = "",
    val feeNoteText: String = "",
    val extraReceiptPaths: String = "",
    /** Lot E — cette ligne est « Mettre de côté ». */
    val isSavingsLine: Boolean = false,
    val savingsTransferredCents: Long = 0L,
    /** Ce mois est déjà viré : le montant de la fiche ne change pas l’avenir. */
    val savingsMonthDone: Boolean = false,
    val error: String? = null,
    val saving: Boolean = false,
    val done: Boolean = false,
    val loading: Boolean = true,
    /** Ligne absente (supprimée pendant l’ouverture). */
    val missing: Boolean = false,
    /**
     * Ancien verrou « aperçu futur » — toujours false (édition OK sur le mois visualisé).
     * Conservé pour ne pas casser les call-sites Compose.
     */
    val futurePreviewReadOnly: Boolean = false,
    /** Intent mail à lancer une fois (consommé par l’UI). */
    val mailDraft: CreditorMailComposer.Draft? = null,
    /** OCR +++ en cours (jamais le montant). */
    val ocrBusy: Boolean = false
)

@HiltViewModel
class EditEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContext: Context,
    private val instanceDao: InstanceDao,
    private val templateDao: TemplateDao,
    private val feeEventDao: ExpenseFeeEventDao,
    private val updateInstance: UpdateInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val receiptPhotos: ReceiptPhotoStore,
    private val receiptOcr: ReceiptCommunicationOcr,
    private val appMetaDao: AppMetaDao,
    private val configureSavings: ConfigureSavingsUseCase
) : ViewModel() {
    private val entryId: String = checkNotNull(savedStateHandle["id"])
    private val isExpenseArg: Boolean = savedStateHandle.get<String>("isExpense") == "true"
    private val _state = MutableStateFlow(EditEntryState(id = entryId, isExpense = isExpenseArg))
    val state: StateFlow<EditEntryState> = _state.asStateFlow()
    private val receiptDraft = EntryFormReceiptDraft()
    private val saveGate = SingleFlightGate()
    private val ocrGate = SingleFlightGate()

    init { viewModelScope.launch { load() } }

    private suspend fun load() {
        if (!isExpenseArg) {
            val e = instanceDao.getIncomeById(entryId) ?: run {
                _state.update {
                    it.copy(loading = false, missing = true)
                }
                return
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
            _state.update {
                it.copy(loading = false, missing = true)
            }
            return
        }
        val interval = e.templateId?.let { tid ->
            templateDao.getExpenseById(tid)?.intervalMonths
        }
        val choice = EntryFormCore.recurrenceChoiceFromInterval(interval)
        val recurrenceInfo = interval?.let { RecurrenceLabel.forIntervalMonths(it) }
            ?: RecurrenceLabel.oneOff()
        val tpl = e.templateId?.let { templateDao.getExpenseById(it) }
        val opening = tpl?.openingRemainingCents
        val paid = e.templateId?.let {
            instanceDao.sumAmountCentsByTemplateAndStatus(it, ExpenseStatus.DONE)
        } ?: 0L
        val fees = tpl?.addedFeesCents ?: 0L
        val remaining = DossierRemaining.remainingCents(opening, paid, fees)
        val instances = e.templateId?.let { instanceDao.expensesByTemplate(it) }.orEmpty()
        val feeRows = e.templateId?.let { tid ->
            feeEventDao.listByTemplate(tid).map { ev ->
                FeeEventRow(
                    id = ev.id,
                    amountCents = ev.amountCents,
                    note = ev.note,
                    dateLabel = FeeEventDateFormat.label(ev.createdAtEpochMs)
                )
            }
        }.orEmpty()
        val savingsId = appMetaDao.get(AppMetaKeys.SAVINGS_TEMPLATE_ID)?.value?.trim().orEmpty()
        val isSavings = tpl != null && tpl.active && tpl.id == savingsId
        val pace = if (isSavings || tpl == null || remaining == null || remaining <= 0L) {
            null
        } else {
            DossierRemaining.pacePhrase(
                forfaitCents = tpl.amountCents,
                remainingCents = remaining,
                intervalMonths = tpl.intervalMonths,
                doneAmounts = instances.filter { it.status == ExpenseStatus.DONE }.map { it.amountCents }
            )
        }
        val transferred = e.templateId
            ?.takeIf { isSavings }
            ?.let { instanceDao.sumAmountCentsByTemplateAndStatus(it, ExpenseStatus.DONE) }
            ?: 0L
        val loadedPaths = EntryFormCore.resolveLoadedReceiptPaths(
            hasTemplate = tpl != null,
            instanceReceiptPath = e.receiptRelativePath,
            templateExtraPaths = tpl?.extraReceiptPaths.orEmpty(),
            isPdf = receiptPhotos::isPdf
        )
        _state.update {
            it.copy(
                loading = false, label = e.label,
                amountText = MoneyFormat.centsToEditable(e.amountCents),
                dayText = e.dayOfMonth.toString(), isAutoDebit = e.isAutoDebit,
                paymentNote = e.paymentNote, receiptRelativePath = loadedPaths.primaryPath,
                recurrenceInfo = recurrenceInfo, recurrence = choice,
                hasTemplate = e.templateId != null,
                causeLabel = tpl?.causeLabel.orEmpty(),
                isAgreement = DossierRemaining.isDossier(opening),
                remainingCents = remaining,
                isSettled = DossierRemaining.isSettled(opening, paid, fees),
                pacePhrase = pace,
                dossierExpanded = DossierRemaining.isDossier(opening),
                iban = tpl?.iban.orEmpty(),
                creditorEmail = tpl?.creditorEmail.orEmpty(),
                addedFeesCents = fees,
                addedFeesNote = tpl?.addedFeesNote.orEmpty(),
                feeEvents = feeRows,
                extraReceiptPaths = loadedPaths.extrasRaw,
                isSavingsLine = isSavings,
                savingsTransferredCents = transferred,
                savingsMonthDone = isSavings && e.status == ExpenseStatus.DONE
            )
        }
    }

    fun setLabel(v: String) {
        if (_state.value.isSavingsLine) return
        _state.update { it.copy(label = v, error = null) }
    }
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
        if (!v && !it.isAgreement) {
            val (expanded, cause, opening) = EntryFormDraftController.collapseConversionDossier(
                expanded = false,
                causeLabel = it.causeLabel,
                openingRemainingText = it.openingRemainingText
            )
            it.copy(
                dossierExpanded = expanded,
                causeLabel = cause,
                openingRemainingText = opening,
                error = null
            )
        } else {
            it.copy(dossierExpanded = v, error = null)
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
            applyToFollowing = EntryFormCore.applyToFollowingOnRecurrenceChange(
                newRecurrence = v,
                hasTemplate = it.hasTemplate,
                currentApplyToFollowing = it.applyToFollowing
            ),
            error = null
        )
    }
    fun attachPreviewBitmap(b: Bitmap) {
        val flags = EntryFormDraftController.attachBitmap(receiptDraft, b)
        _state.update {
            it.copy(
                hasPendingPhoto = flags.hasPendingPhoto,
                hasPendingPdf = flags.hasPendingPdf,
                receiptCleared = false,
                error = null
            )
        }
    }

    fun attachPendingPdf(bytes: ByteArray) {
        val flags = EntryFormDraftController.attachPdf(receiptDraft, bytes)
        _state.update {
            it.copy(
                hasPendingPhoto = flags.hasPendingPhoto,
                hasPendingPdf = flags.hasPendingPdf,
                error = null
            )
        }
    }

    fun setPhotoDecodeError() = _state.update {
        it.copy(error = EntryFormDraftController.photoDecodeError())
    }

    fun setCameraCaptureError(message: String = EntryFormMessages.CAMERA_CAPTURE_FAILED) =
        _state.update { it.copy(error = message) }

    fun setPdfReadError(message: String = EntryFormMessages.PDF_READ_ERROR_DEFAULT) =
        _state.update { it.copy(error = EntryFormDraftController.pdfReadError(message)) }

    fun clearReceiptPhoto() {
        val cleared = EntryFormDraftController.clearEditReceiptPhoto(receiptDraft)
        _state.update {
            it.copy(
                hasPendingPhoto = cleared.flags.hasPendingPhoto,
                hasPendingPdf = cleared.flags.hasPendingPdf,
                receiptRelativePath = cleared.receiptRelativePath,
                receiptCleared = cleared.receiptCleared,
                error = null
            )
        }
    }

    fun clearPendingPdf() {
        val flags = EntryFormDraftController.clearPdf(receiptDraft)
        _state.update {
            it.copy(
                hasPendingPhoto = flags.hasPendingPhoto,
                hasPendingPdf = flags.hasPendingPdf,
                error = null
            )
        }
    }

    fun isPdfPath(relativePath: String): Boolean = receiptPhotos.isPdf(relativePath)

    /** Ouvre photo ou PDF hors de l’app (lecteur système). Null si introuvable. */
    suspend fun viewReceiptIntent(relativePath: String): Intent? {
        val path = relativePath.trim()
        if (path.isEmpty()) return null
        val uri = receiptPhotos.viewUri(path) ?: return null
        return receiptPhotos.viewIntent(uri, path)
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
        when (
            val outcome = EntryFormDraftController.epcOutcomeFromRaw(_state.value.toPaymentFields(), raw)
        ) {
            EntryFormDraftController.EpcOutcome.Invalid ->
                _state.update { it.copy(error = EntryFormMessages.INVALID_EPC_QR) }
            is EntryFormDraftController.EpcOutcome.Ok -> {
                val patch = outcome.patch
                _state.update {
                    it.copy(
                        label = patch.label,
                        amountText = patch.amountText,
                        paymentNote = patch.paymentNote,
                        iban = patch.iban,
                        error = null,
                        copyMessage = patch.copyMessage
                    )
                }
            }
        }
    }

    fun applyEpc(payment: EpcPayment) {
        _state.update {
            when (
                val outcome = EntryFormDraftController.epcOutcomeFromPayment(
                    it.toPaymentFields(),
                    payment
                )
            ) {
                EntryFormDraftController.EpcOutcome.Invalid ->
                    it.copy(error = EntryFormMessages.INVALID_EPC_QR)
                is EntryFormDraftController.EpcOutcome.Ok -> {
                    val patch = outcome.patch
                    it.copy(
                        label = patch.label,
                        amountText = patch.amountText,
                        paymentNote = patch.paymentNote,
                        iban = patch.iban,
                        error = null,
                        copyMessage = patch.copyMessage
                    )
                }
            }
        }
    }

    private fun EditEntryState.toPaymentFields() = EntryFormDraftController.paymentFields(
        label = label,
        amountText = amountText,
        paymentNote = paymentNote,
        iban = iban,
        copyMessage = copyMessage,
        openingRemainingText = openingRemainingText,
        dossierExpanded = dossierExpanded
    )

    fun setIban(v: String) = _state.update { it.copy(iban = v, error = null) }
    fun setCreditorEmail(v: String) = _state.update { it.copy(creditorEmail = v, error = null) }
    fun setFeeAddText(v: String) = _state.update { it.copy(feeAddText = v, error = null) }
    fun setFeeNoteText(v: String) = _state.update { it.copy(feeNoteText = v, error = null) }

    fun copyIban() {
        val outcome = EntryFormCreditorCopy.copyIban(appContext, _state.value.iban)
        _state.update {
            it.copy(
                copyMessage = outcome.copyMessage,
                error = outcome.error
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
            _state.update { it.copy(error = "Indiquez d’abord l’e-mail (pour Écrire).") }
            return
        }
        if (!CreditorMailComposer.looksLikeEmail(draft.to)) {
            _state.update { it.copy(error = "Vérifiez l’e-mail.", mailDraft = null) }
            return
        }
        _state.update { it.copy(mailDraft = draft, error = null) }
    }

    fun consumeMailDraft() = _state.update { it.copy(mailDraft = null) }

    /** SENDTO introuvable → fallback SEND déjà lancé + e-mail créancier copié. */
    fun mailLaunchUsedFallback() = _state.update {
        it.copy(
            mailDraft = null,
            error = null,
            copyMessage = be.monmoisclair.app.domain.SupportMailIntents.ADDRESS_COPIED
        )
    }

    fun mailLaunchFailed() = _state.update { s ->
        val email = s.mailDraft?.to?.trim().orEmpty().ifBlank { s.creditorEmail.trim() }
        if (email.isNotEmpty()) {
            SensitiveClipboard.setPlainText(appContext, "E-mail", email)
        }
        s.copy(
            mailDraft = null,
            error = if (email.isNotEmpty()) {
                be.monmoisclair.app.domain.SupportMailIntents.ADDRESS_COPIED
            } else {
                be.monmoisclair.app.domain.SupportMailIntents.NO_MAIL_APP
            },
            copyMessage = null
        )
    }

    fun copyPaymentNote() {
        val result = EntryFormPaymentNoteCopy.copy(appContext, _state.value.paymentNote)
        if (!result.ok) {
            _state.update { it.copy(error = result.message, copyMessage = null) }
            return
        }
        _state.update { it.copy(copyMessage = result.message, error = null) }
    }

    /**
     * Lit une communication +++ sur la photo (OCR hors ligne).
     * Ne touche jamais au montant.
     */
    fun readCommunicationFromPhoto() {
        if (_state.value.ocrBusy || _state.value.futurePreviewReadOnly) return
        if (!_state.value.isExpense || _state.value.isSavingsLine) return
        if (!ocrGate.tryEnter()) return
        viewModelScope.launch {
            try {
            _state.update {
                it.copy(
                    ocrBusy = true,
                    error = null,
                    copyMessage = EntryFormDraftController.ocrBusyMessage()
                )
            }
            val fromPending = receiptDraft.peekBitmap()
            val path = _state.value.receiptRelativePath
            val bmp = fromPending
                ?: if (receiptPhotos.isPdf(path)) {
                    null
                } else {
                    receiptPhotos.decodeBitmap(path)
                }
            if (bmp == null) {
                _state.update {
                    it.copy(
                        ocrBusy = false,
                        copyMessage = null,
                        error = BelgianStructuredCommunication.OCR_NO_PHOTO
                    )
                }
                return@launch
            }
            when (
                val outcome = EntryFormDraftController.resolveOcr(
                    _state.value.toPaymentFields(),
                    runCatching { receiptOcr.findStructuredCommunication(bmp) }.getOrNull()
                )
            ) {
                is EntryFormDraftController.OcrOutcome.NotFound ->
                    _state.update {
                        it.copy(
                            ocrBusy = false,
                            copyMessage = outcome.fields.copyMessage,
                            error = BelgianStructuredCommunication.OCR_NOT_FOUND
                        )
                    }
                is EntryFormDraftController.OcrOutcome.Applied ->
                    _state.update {
                        it.copy(
                            ocrBusy = false,
                            paymentNote = outcome.fields.paymentNote,
                            copyMessage = outcome.fields.copyMessage,
                            error = null
                        )
                    }
            }
            } finally {
                ocrGate.exit()
            }
        }
    }

    fun save() {
        if (!EntryFormCore.canStartSave(_state.value.saving, _state.value.done)) return
        if (!saveGate.tryEnter()) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
            val s = _state.value
            var path = s.receiptRelativePath
            var extraAppend = ""
            var documentsHint: String? = null
            val cleared = s.receiptCleared && !receiptDraft.hasPhoto
            // null = inchangé ; -1 = une fois ; 1/3/12 = intervalle (crée le modèle si besoin).
            val interval = EntryFormCore.editIntervalMonths(
                isExpense = s.isExpense,
                recurrence = s.recurrence,
                hasTemplate = s.hasTemplate
            )
            val wantOpening = s.isExpense && s.dossierExpanded && !s.isAgreement
            val prep = EntryFormCore.prepareSharedSave(
                amountText = s.amountText,
                dayText = s.dayText,
                wantOpening = wantOpening,
                openingRemainingText = s.openingRemainingText,
                recurrence = s.recurrence
            )
            if (prep is EntryFormCore.SharedSavePrep.Err) {
                _state.update { it.copy(saving = false, error = prep.message) }
                return@launch
            }
            val validated = (prep as EntryFormCore.SharedSavePrep.Ok).validated
            val cents = validated.cents
            val day = validated.day
            val openingCents = validated.openingCents
            val feeParsed = EntryFormCore.parseFeeAdd(s.isAgreement, s.feeAddText, s.feeNoteText)
            if (feeParsed is EntryFormCore.FeeAdd.Err) {
                _state.update { it.copy(saving = false, error = feeParsed.message) }
                return@launch
            }
            val feeAdd = (feeParsed as EntryFormCore.FeeAdd.Ok).cents
            val owner = if (s.applyToFollowing || s.isSavingsLine) {
                instanceDao.getExpenseById(s.id)?.templateId ?: s.id
            } else {
                s.id
            }
            receiptDraft.peekBitmap()?.let { bmp ->
                if (s.isAgreement && path.isNotBlank() && !cleared) {
                    val saved = receiptPhotos.saveBitmap(
                        "$owner-extra-${AppClock.nowEpochMs()}",
                        bmp
                    )
                    extraAppend = saved.relativePath
                    if (!saved.documentsCopyOk) {
                        documentsHint =
                            "Photo enregistrée. La copie dans Documents a échoué — elle sera retentée."
                    }
                } else {
                    val saved = receiptPhotos.saveBitmap(owner, bmp)
                    path = saved.relativePath
                    if (!saved.documentsCopyOk) {
                        documentsHint =
                            "Photo enregistrée. La copie dans Documents a échoué — elle sera retentée."
                    }
                }
            }
            // PDF = courrier en plus sur toute facture. Jamais à la place de la photo seule
            // (sauf facture « une fois » sans photo : le PDF est alors la seule pièce).
            var extrasReplace: String? = when {
                s.hasTemplate -> s.extraReceiptPaths
                s.isAgreement -> s.extraReceiptPaths
                else -> null
            }
            receiptDraft.peekPdf()?.let { pdf ->
                val expenseRow = instanceDao.getExpenseById(s.id)
                val ownerId = expenseRow?.templateId ?: s.id
                try {
                    val saved = receiptPhotos.savePdfBytes(
                        "$ownerId-pdf-${AppClock.nowEpochMs()}",
                        pdf
                    )
                    if (s.hasTemplate) {
                        extrasReplace = ReceiptPathList.append(
                            extrasReplace.orEmpty(),
                            saved.relativePath
                        )
                    } else {
                        // Une fois : photo (+ PDF) dans le même champ instance.
                        extrasReplace = null
                        extraAppend = ""
                        path = EntryFormCore.encodeOneOffReceiptPaths(
                            primaryPath = path,
                            existingExtras = s.extraReceiptPaths,
                            cleared = cleared,
                            newPdfRelativePath = saved.relativePath
                        )
                    }
                    if (!saved.documentsCopyOk && documentsHint == null) {
                        documentsHint =
                            "PDF enregistré. La copie dans Documents a échoué — elle sera retentée."
                    }
                } catch (e: BackupFailure.Io) {
                    _state.update {
                        it.copy(saving = false, error = e.message ?: "PDF impossible à enregistrer.")
                    }
                    return@launch
                }
            }
            // Facture une fois : recomposer photo + courriers déjà en state (sans nouveau PDF).
            if (!s.hasTemplate && !receiptDraft.hasPdf) {
                path = EntryFormCore.encodeOneOffReceiptPaths(
                    primaryPath = path,
                    existingExtras = s.extraReceiptPaths,
                    cleared = cleared
                )
            }
            val syncCreditor = s.isExpense && (s.isAgreement || s.dossierExpanded)
            val labelToSave = if (s.isSavingsLine) {
                ConfigureSavingsUseCase.LABEL
            } else {
                s.label
            }
            when (
                val result = updateInstance(
                    id = s.id,
                    isExpense = s.isExpense,
                    label = labelToSave,
                    amountCents = cents,
                    dayOfMonth = day,
                    applyToFollowingMonths = s.applyToFollowing || s.isSavingsLine,
                    isAutoDebit = s.isExpense && s.isAutoDebit && !s.isSavingsLine,
                    paymentNote = s.paymentNote,
                    receiptRelativePath = path,
                    intervalMonths = if (s.isSavingsLine) 1 else interval,
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
                    feeAddCents = feeAdd,
                    feeNote = s.feeNoteText,
                    extraReceiptAppend = extraAppend,
                    extraReceiptPathsReplace = extrasReplace,
                    keepDoneAmount = s.isSavingsLine
                )
            ) {
                UpdateInstanceResult.Success -> {
                    val flags = EntryFormDraftController.clearAllAttachments(receiptDraft)
                    _state.update {
                        it.copy(
                            saving = false,
                            done = true,
                            hasPendingPhoto = flags.hasPendingPhoto,
                            hasPendingPdf = flags.hasPendingPdf,
                            copyMessage = documentsHint
                        )
                    }
                }
                UpdateInstanceResult.NotFound ->
                    _state.update { it.copy(saving = false, error = "Cette ligne n’est plus là.") }
                is UpdateInstanceResult.InvalidInput ->
                    _state.update { it.copy(saving = false, error = result.message) }
            }
            } finally {
                saveGate.exit()
            }
        }
    }

    fun delete() {
        if (!EntryFormCore.canStartSave(_state.value.saving, _state.value.done)) return
        if (!saveGate.tryEnter()) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
            if (_state.value.isSavingsLine) {
                when (configureSavings.remove()) {
                    ConfigureSavingsResult.Removed ->
                        _state.update { it.copy(saving = false, done = true) }
                    else -> _state.update {
                        it.copy(
                            saving = false,
                            error = "La ligne n’a pas pu être retirée. Réessayez depuis Réglages."
                        )
                    }
                }
                return@launch
            }
            when (deleteInstance(_state.value.id, _state.value.isExpense, _state.value.applyToFollowing)) {
                DeleteInstanceResult.Success ->
                    _state.update { it.copy(saving = false, done = true) }
                DeleteInstanceResult.NotFound ->
                    _state.update { it.copy(saving = false, error = "Cette ligne n’est plus là.") }
            }
            } finally {
                saveGate.exit()
            }
        }
    }
}
