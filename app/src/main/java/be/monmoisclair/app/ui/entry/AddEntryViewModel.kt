package be.monmoisclair.app.ui.entry

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.data.backup.BackupFailure
import be.monmoisclair.app.data.receipt.ReceiptCommunicationOcr
import be.monmoisclair.app.data.receipt.ReceiptPhotoStore
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.domain.EpcPayment
import be.monmoisclair.app.domain.ReceiptPathList
import be.monmoisclair.app.domain.RecurrenceAnchor
import be.monmoisclair.app.domain.SimilarExpenseMatcher
import be.monmoisclair.app.domain.usecase.AddEntryResult
import be.monmoisclair.app.domain.usecase.AddEntryUseCase
import be.monmoisclair.app.domain.usecase.CreatePendingLetterResult
import be.monmoisclair.app.domain.usecase.CreatePendingLetterUseCase
import be.monmoisclair.app.domain.usecase.FindSimilarExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

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
    val hasPendingPdf: Boolean = false,
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
    val ocrBusy: Boolean = false,
    val formTitle: String = "Nouvelle facture ou revenu",
    val targetYearMonth: YearMonth = AppClock.currentYearMonth(),
    /** Doublon probable dans ce mois — confirmation avant insert. */
    val duplicateConfirm: SimilarExpenseMatcher.Match? = null
) {
    val needsNextDueMonth: Boolean
        get() = recurrence == RecurrenceChoice.Every3Months || recurrence == RecurrenceChoice.Yearly
}

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContext: Context,
    private val addEntry: AddEntryUseCase,
    private val findSimilarExpenses: FindSimilarExpensesUseCase,
    private val createPendingLetter: CreatePendingLetterUseCase,
    private val receiptPhotos: ReceiptPhotoStore,
    private val receiptOcr: ReceiptCommunicationOcr,
    private val instanceDao: InstanceDao,
    private val templateDao: TemplateDao,
    private val mutatingRepository: MutatingRepository
) : ViewModel() {
    private val addTarget = EntryFormCore.resolveAddFormTarget(savedStateHandle.get<String>("ym"))
    private val targetYm: YearMonth = addTarget.yearMonth
    private val _state = MutableStateFlow(
        EntryFormState(
            targetYearMonth = targetYm,
            formTitle = addTarget.formTitle,
            nextDueMonth = addTarget.nextDueMonth,
            error = addTarget.initialError
        )
    )
    val state: StateFlow<EntryFormState> = _state.asStateFlow()
    private val receiptDraft = EntryFormReceiptDraft()
    /** Évite un 2ᵉ insert si la photo a échoué après un 1ᵉr enregistrement OK. */
    @Volatile private var createdInstanceId: String? = null
    /** Anti-course : double Enregistrer / confirm doublon en parallèle. */
    private val saveGate = AtomicBoolean(false)

    fun setLabel(v: String) = _state.update { it.copy(label = v, error = null) }
    fun setAmount(v: String) = _state.update { it.copy(amountText = v, error = null) }
    fun setDay(v: String) = _state.update { it.copy(dayText = v, error = null) }
    fun setExpense(v: Boolean) {
        val flags = EntryFormDraftController.applyExpenseType(receiptDraft, v)
        _state.update {
            it.copy(
                isExpense = v,
                isAutoDebit = if (v) it.isAutoDebit else false,
                hasPendingPdf = flags.hasPendingPdf,
                hasPendingPhoto = flags.hasPendingPhoto,
                error = null
            )
        }
    }
    fun setRecurrence(v: RecurrenceChoice) = _state.update { it.copy(recurrence = v) }
    fun setNextDueMonth(m: Int) = _state.update { it.copy(nextDueMonth = m.coerceIn(1, 12)) }
    fun setAutoDebit(v: Boolean) = _state.update { it.copy(isAutoDebit = v) }
    fun setPaymentNote(v: String) = _state.update {
        it.copy(paymentNote = v, error = null, copyMessage = null)
    }

    fun dismissDuplicateConfirm() =
        _state.update { it.copy(duplicateConfirm = null, saving = false) }

    fun confirmDuplicateAndSave() {
        _state.update { it.copy(duplicateConfirm = null) }
        save(skipDuplicateCheck = true)
    }
    /** Collapse = annuler : on efface cause / reste pour ne pas créer un dossier par erreur. */
    fun setDossierExpanded(v: Boolean) = _state.update {
        val (expanded, cause, opening) = EntryFormDraftController.collapseConversionDossier(
            expanded = v,
            causeLabel = it.causeLabel,
            openingRemainingText = it.openingRemainingText
        )
        it.copy(
            dossierExpanded = expanded,
            causeLabel = cause,
            openingRemainingText = opening,
            error = null
        )
    }
    fun setCauseLabel(v: String) = _state.update { it.copy(causeLabel = v, error = null) }
    fun setOpeningRemainingText(v: String) = _state.update {
        it.copy(openingRemainingText = v, error = null)
    }
    fun attachPreviewBitmap(b: Bitmap) {
        val flags = EntryFormDraftController.attachBitmap(receiptDraft, b)
        _state.update {
            it.copy(
                hasPendingPhoto = flags.hasPendingPhoto,
                hasPendingPdf = flags.hasPendingPdf,
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

    fun setPhotoDecodeError() = _state.update {
        it.copy(error = EntryFormDraftController.photoDecodeError())
    }

    fun setCameraCaptureError(message: String = EntryFormMessages.CAMERA_CAPTURE_FAILED) =
        _state.update { it.copy(error = message) }

    fun setPdfReadError(message: String = EntryFormMessages.PDF_READ_ERROR_DEFAULT) =
        _state.update { it.copy(error = EntryFormDraftController.pdfReadError(message)) }

    fun copyPaymentNote() {
        val result = EntryFormPaymentNoteCopy.copy(appContext, _state.value.paymentNote)
        if (!result.ok) {
            _state.update { it.copy(error = result.message, copyMessage = null) }
            return
        }
        _state.update { it.copy(copyMessage = result.message, error = null) }
    }

    /** OCR +++ sur la photo en attente — jamais le montant. */
    fun readCommunicationFromPhoto() {
        if (_state.value.ocrBusy || !_state.value.isExpense) return
        val bmp = receiptDraft.peekBitmap()
        if (bmp == null) {
            _state.update {
                it.copy(error = BelgianStructuredCommunication.OCR_NO_PHOTO, copyMessage = null)
            }
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    ocrBusy = true,
                    error = null,
                    copyMessage = EntryFormDraftController.ocrBusyMessage()
                )
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
        }
    }

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
                        isExpense = true,
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
                        isExpense = true,
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

    private fun EntryFormState.toPaymentFields() = EntryFormDraftController.paymentFields(
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

    /**
     * Lot D — range photo / qui / pour quoi sans montant (pas dans le Reste).
     * Visible seulement si le bloc accord est ouvert et le reste dû est vide.
     */
    fun saveAsPendingLetter() {
        if (!EntryFormCore.canStartSave(_state.value.saving, _state.value.saved)) return
        if (!saveGate.compareAndSet(false, true)) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                val s = _state.value
                when (
                    val gate = EntryFormCore.pendingLetterCheck(
                        isExpense = s.isExpense,
                        dossierExpanded = s.dossierExpanded,
                        openingRemainingText = s.openingRemainingText,
                        whoLabel = s.label
                    )
                ) {
                    EntryFormCore.PendingLetterCheck.AbortSilent -> {
                        _state.update { it.copy(saving = false) }
                        return@launch
                    }
                    is EntryFormCore.PendingLetterCheck.Abort -> {
                        _state.update { it.copy(saving = false, error = gate.message) }
                        return@launch
                    }
                    EntryFormCore.PendingLetterCheck.Proceed -> Unit
                }
                val who = s.label.trim()
                when (
                    val result = createPendingLetter(
                        who = who,
                        what = s.causeLabel,
                        paymentNote = s.paymentNote,
                        iban = s.iban,
                        creditorEmail = s.creditorEmail
                    )
                ) {
                    is CreatePendingLetterResult.InvalidInput -> {
                        _state.update { it.copy(saving = false, error = result.message) }
                    }
                    is CreatePendingLetterResult.Success -> {
                        val bmp = receiptDraft.peekBitmap()
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
                            receiptDraft.clearBitmap()
                        }
                        _state.update { it.copy(saving = false, saved = true, error = null) }
                    }
                }
            } finally {
                saveGate.set(false)
            }
        }
    }

    fun save(skipDuplicateCheck: Boolean = false) {
        if (!EntryFormCore.canStartSave(_state.value.saving, _state.value.saved)) return
        if (!saveGate.compareAndSet(false, true)) return
        _state.update { it.copy(saving = true, error = null, duplicateConfirm = null) }
        viewModelScope.launch {
            try {
                val existingId = createdInstanceId
                if (existingId != null) {
                    retryAttachments(existingId)
                    return@launch
                }
                val s = _state.value
            val wantOpening = s.isExpense && s.dossierExpanded
            val prep = EntryFormCore.prepareSharedSave(
                amountText = s.amountText,
                dayText = s.dayText,
                wantOpening = wantOpening,
                openingRemainingText = s.openingRemainingText,
                recurrence = s.recurrence,
                creditorGuard = EntryFormCore.CreditorGuard(
                    isExpense = s.isExpense,
                    dossierExpanded = s.dossierExpanded,
                    iban = s.iban,
                    creditorEmail = s.creditorEmail
                )
            )
            if (prep is EntryFormCore.SharedSavePrep.Err) {
                _state.update { it.copy(saving = false, error = prep.message) }
                return@launch
            }
            val validated = (prep as EntryFormCore.SharedSavePrep.Ok).validated
            val cents = validated.cents
            val day = validated.day
            val openingCents = validated.openingCents
            if (
                s.isExpense &&
                !skipDuplicateCheck
            ) {
                val ledgerId = AppClock.ledgerId(s.targetYearMonth)
                val similar = findSimilarExpenses.findInLedger(
                    ledgerId = ledgerId,
                    label = s.label,
                    amountCents = cents,
                    dayOfMonth = day,
                    causeLabel = if (s.dossierExpanded) s.causeLabel else ""
                )
                if (similar.isNotEmpty()) {
                    _state.update {
                        it.copy(
                            saving = false,
                            duplicateConfirm = similar.first()
                        )
                    }
                    return@launch
                }
            }
            val recurring = s.recurrence != RecurrenceChoice.Once
            val interval = EntryFormCore.intervalMonths(s.recurrence)
            val anchor = when {
                !recurring -> null
                s.needsNextDueMonth ->
                    RecurrenceAnchor.nextYearMonth(s.nextDueMonth, from = s.targetYearMonth)
                else -> s.targetYearMonth
            }
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
                    createdInstanceId = result.instanceId
                    // documentsOk : la copie SAF peut échouer ; la photo locale suffit pour rester.
                    val (photoOk, _) = attachPendingPhoto(result.instanceId)
                    val pdfOk = attachPendingPdf(
                        result.instanceId,
                        recurring = s.recurrence != RecurrenceChoice.Once
                    )
                    // Garder le bitmap / PDF en échec pour un 2ᵉ Enregistrer (retry).
                    if (photoOk) receiptDraft.clearBitmap()
                    if (pdfOk) receiptDraft.clearPdf()
                    val attachOk = photoOk && pdfOk
                    _state.update {
                        it.copy(
                            saving = false,
                            saved = attachOk,
                            hasPendingPhoto = receiptDraft.hasPhoto,
                            hasPendingPdf = receiptDraft.hasPdf,
                            error = when {
                                attachOk -> null
                                !photoOk && !pdfOk ->
                                    "Facture enregistrée. Photo et PDF n’ont pas suivi — " +
                                        "réessayez Enregistrer."
                                !photoOk ->
                                    "Facture enregistrée. La photo n’a pas suivi — " +
                                        "reprenez-en une, puis Enregistrer."
                                else ->
                                    "Facture enregistrée. Le PDF n’a pas suivi — " +
                                        "réessayez Enregistrer."
                            }
                        )
                    }
                }
                is AddEntryResult.InvalidInput ->
                    _state.update { it.copy(saving = false, error = result.message) }
            }
            } finally {
                saveGate.set(false)
            }
        }
    }

    private suspend fun retryAttachments(instanceId: String) {
        val hasPhoto = receiptDraft.hasPhoto
        val hasPdf = receiptDraft.hasPdf
        if (!hasPhoto && !hasPdf) {
            _state.update {
                it.copy(
                    error = EntryFormCore.ATTACH_RETRY_NEED,
                    saving = false
                )
            }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        val expense = instanceDao.getExpenseById(instanceId)
        val recurring = expense?.templateId != null ||
            (expense == null && templateDao.getExpenseById(instanceId) != null)
        val photoOk = if (hasPhoto) attachPendingPhoto(instanceId).first else true
        val pdfOk = if (hasPdf) attachPendingPdf(instanceId, recurring = recurring) else true
        if (photoOk) receiptDraft.clearBitmap()
        if (pdfOk) receiptDraft.clearPdf()
        val flags = EntryFormDraftController.peekFlags(receiptDraft)
        val ok = photoOk && pdfOk
        _state.update {
            it.copy(
                saving = false,
                saved = ok,
                hasPendingPhoto = flags.hasPendingPhoto,
                hasPendingPdf = flags.hasPendingPdf,
                error = EntryFormCore.attachRetryError(photoOk, pdfOk)
            )
        }
    }

    /** @return Pair(ok, documentsCopyOk) — ok = écriture locale réussie. */
    private suspend fun attachPendingPhoto(instanceId: String): Pair<Boolean, Boolean> {
        val bmp = receiptDraft.peekBitmap() ?: return true to true
        return runCatching {
            var documentsOk = true
            val expense = instanceDao.getExpenseById(instanceId)
            if (expense != null) {
                val instanceSave = receiptPhotos.saveBitmap(expense.id, bmp)
                documentsOk = documentsOk && instanceSave.documentsCopyOk
                val templateId = expense.templateId
                val canonical = templateId?.let { receiptPhotos.saveBitmap(it, bmp) }
                if (canonical != null) documentsOk = documentsOk && canonical.documentsCopyOk
                mutatingRepository.mutate {
                    instanceDao.getExpenseById(expense.id)?.let {
                        instanceDao.updateExpense(
                            it.copy(receiptRelativePath = instanceSave.relativePath)
                        )
                    }
                    if (templateId != null && canonical != null) {
                        templateDao.getExpenseById(templateId)?.let { tpl ->
                            templateDao.updateExpense(
                                tpl.copy(receiptRelativePath = canonical.relativePath)
                            )
                        }
                    }
                }
            } else {
                val canonical = receiptPhotos.saveBitmap(instanceId, bmp)
                documentsOk = canonical.documentsCopyOk
                mutatingRepository.mutate {
                    templateDao.getExpenseById(instanceId)?.let { tpl ->
                        templateDao.updateExpense(
                            tpl.copy(receiptRelativePath = canonical.relativePath)
                        )
                    }
                }
            }
            true to documentsOk
        }.getOrElse { false to false }
    }

    /**
     * PDF à côté de la photo dès l’ajout (même règle que l’édition) :
     * facture qui revient → courriers du modèle ; une fois → même champ instance.
     */
    private suspend fun attachPendingPdf(instanceId: String, recurring: Boolean): Boolean {
        val pdf = receiptDraft.peekPdf() ?: return true
        return try {
            val expense = instanceDao.getExpenseById(instanceId)
            val templateOnly = if (expense == null && recurring) {
                templateDao.getExpenseById(instanceId)
            } else {
                null
            }
            if (expense == null && templateOnly == null) return false
            val ownerId = expense?.templateId
                ?: expense?.id
                ?: templateOnly!!.id
            val saved = receiptPhotos.savePdfBytes(
                "$ownerId-pdf-${AppClock.nowEpochMs()}",
                pdf
            )
            var linked = false
            mutatingRepository.mutate {
                if (expense != null && expense.templateId != null) {
                    templateDao.getExpenseById(expense.templateId)?.let { tpl ->
                        templateDao.updateExpense(
                            tpl.copy(
                                extraReceiptPaths = ReceiptPathList.append(
                                    tpl.extraReceiptPaths,
                                    saved.relativePath
                                )
                            )
                        )
                        linked = true
                    }
                } else if (expense != null) {
                    val merged = ReceiptPathList.append(
                        expense.receiptRelativePath,
                        saved.relativePath
                    )
                    instanceDao.updateExpense(expense.copy(receiptRelativePath = merged))
                    linked = true
                } else if (templateOnly != null) {
                    templateDao.updateExpense(
                        templateOnly.copy(
                            extraReceiptPaths = ReceiptPathList.append(
                                templateOnly.extraReceiptPaths,
                                saved.relativePath
                            )
                        )
                    )
                    linked = true
                }
            }
            linked
        } catch (_: BackupFailure.Io) {
            false
        } catch (_: Exception) {
            false
        }
    }
}
