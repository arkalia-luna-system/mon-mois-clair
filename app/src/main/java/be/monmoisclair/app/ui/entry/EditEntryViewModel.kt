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
import be.monmoisclair.app.domain.EpcQrParser
import be.monmoisclair.app.domain.MonthNavigation
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
import be.monmoisclair.app.ui.format.FeeEventDateFormat
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
     * « Encore environ N mois si… » — null si un FAIT a divergé, si c’est soldé,
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
    /** Ledger d’aperçu futur : lecture seule (pas d’Enregistrer / Supprimer). */
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
    @Volatile private var pendingBitmap: Bitmap? = null
    @Volatile private var pendingPdfBytes: ByteArray? = null

    init { viewModelScope.launch { load() } }

    private suspend fun load() {
        if (!isExpenseArg) {
            val e = instanceDao.getIncomeById(entryId) ?: run {
                _state.update {
                    it.copy(loading = false, missing = true)
                }
                return
            }
            val futureRo = isFuturePreviewLedger(e.ledgerId)
            _state.update {
                it.copy(
                    loading = false, label = e.label,
                    amountText = MoneyFormat.centsToEditable(e.amountCents),
                    dayText = e.dayOfMonth.toString(),
                    recurrenceInfo = if (e.templateId != null) RecurrenceLabel.forIntervalMonths(1) else RecurrenceLabel.oneOff(),
                    recurrence = if (e.templateId != null) RecurrenceChoice.Monthly else RecurrenceChoice.Once,
                    hasTemplate = e.templateId != null,
                    futurePreviewReadOnly = futureRo,
                    error = if (futureRo) {
                        "Aperçu seulement — ouvrez le mois en cours pour modifier."
                    } else {
                        null
                    }
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
                doneAmounts = instances.filter { it.status == ExpenseStatus.DONE }.map { it.amountCents },
                todoAmounts = instances.filter { it.status == ExpenseStatus.TODO }.map { it.amountCents }
            )
        }
        val transferred = e.templateId
            ?.takeIf { isSavings }
            ?.let { instanceDao.sumAmountCentsByTemplateAndStatus(it, ExpenseStatus.DONE) }
            ?: 0L
        val futureRo = isFuturePreviewLedger(e.ledgerId)
        // Modèle : extras sur le template. Une fois : photo + PDF dans receiptRelativePath.
        val isPdf: (String) -> Boolean = receiptPhotos::isPdf
        val primaryPath: String
        val extrasRaw: String
        if (tpl != null) {
            primaryPath = e.receiptRelativePath
            extrasRaw = tpl.extraReceiptPaths
        } else {
            primaryPath = ReceiptPathList.primaryPhotoOrFirst(e.receiptRelativePath, isPdf)
            extrasRaw = ReceiptPathList.encode(
                ReceiptPathList.extrasBesidesPrimary(e.receiptRelativePath, isPdf)
            )
        }
        _state.update {
            it.copy(
                loading = false, label = e.label,
                amountText = MoneyFormat.centsToEditable(e.amountCents),
                dayText = e.dayOfMonth.toString(), isAutoDebit = e.isAutoDebit,
                paymentNote = e.paymentNote, receiptRelativePath = primaryPath,
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
                futurePreviewReadOnly = futureRo,
                error = if (futureRo) {
                    "Aperçu seulement — ouvrez le mois en cours pour modifier."
                } else {
                    null
                },
                feeEvents = feeRows,
                extraReceiptPaths = extrasRaw,
                isSavingsLine = isSavings,
                savingsTransferredCents = transferred,
                savingsMonthDone = isSavings && e.status == ExpenseStatus.DONE
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
        _state.update {
            it.copy(
                hasPendingPhoto = true,
                receiptCleared = false,
                error = null
            )
        }
    }

    fun attachPendingPdf(bytes: ByteArray) {
        pendingPdfBytes = bytes
        _state.update {
            it.copy(
                hasPendingPdf = true,
                error = null
            )
        }
    }

    fun setPhotoDecodeError() = _state.update {
        it.copy(error = "La photo n’a pas pu être lue. Réessayez.")
    }

    fun setPdfReadError(message: String = "Ce PDF n’a pas pu être lu. Réessayez.") =
        _state.update { it.copy(error = message) }

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

    fun clearPendingPdf() {
        pendingPdfBytes = null
        _state.update { it.copy(hasPendingPdf = false, error = null) }
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
        val compact = BelgianStructuredCommunication.ibanForClipboard(iban)
        if (!BelgianStructuredCommunication.ibanChecksumOk(compact)) {
            _state.update {
                it.copy(error = "Vérifiez l’IBAN sur le courrier.", copyMessage = null)
            }
            return
        }
        SensitiveClipboard.setPlainText(appContext, "IBAN", compact)
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

    fun mailLaunchFailed() = _state.update {
        it.copy(
            mailDraft = null,
            error = "Aucune application Mail trouvée sur ce téléphone."
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
        SensitiveClipboard.setPlainText(appContext, "Communication", prepared.clipboardText)
        _state.update {
            it.copy(copyMessage = prepared.userMessage, error = null)
        }
    }

    /**
     * Lit une communication +++ sur la photo (OCR hors ligne).
     * Ne touche jamais au montant.
     */
    fun readCommunicationFromPhoto() {
        if (_state.value.ocrBusy || _state.value.futurePreviewReadOnly) return
        if (!_state.value.isExpense || _state.value.isSavingsLine) return
        viewModelScope.launch {
            _state.update {
                it.copy(ocrBusy = true, error = null, copyMessage = BelgianStructuredCommunication.OCR_BUSY)
            }
            val fromPending = pendingBitmap
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
            val found = runCatching { receiptOcr.findStructuredCommunication(bmp) }.getOrNull()
            if (found.isNullOrBlank()) {
                _state.update {
                    it.copy(
                        ocrBusy = false,
                        copyMessage = null,
                        error = BelgianStructuredCommunication.OCR_NOT_FOUND
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        ocrBusy = false,
                        paymentNote = found,
                        copyMessage = BelgianStructuredCommunication.OCR_FOUND,
                        error = null
                    )
                }
            }
        }
    }

    fun save() {
        if (_state.value.saving || _state.value.done) return
        if (_state.value.futurePreviewReadOnly) {
            _state.update {
                it.copy(
                    error = "Aperçu seulement — ouvrez le mois en cours pour modifier."
                )
            }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val s = _state.value
            if (MoneyFormat.tooManyFractionDigits(s.amountText)) {
                _state.update { it.copy(saving = false, error = MoneyFormat.FRACTION_HINT) }
                return@launch
            }
            val cents = MoneyFormat.parseToCents(s.amountText)
            val day = s.dayText.trim().toIntOrNull()
            if (cents == null || day == null) {
                _state.update {
                    it.copy(saving = false, error = "Vérifiez le montant et le jour (1 à 31).")
                }
                return@launch
            }
            if (MoneyFormat.isOverLineMax(cents)) {
                _state.update { it.copy(saving = false, error = MoneyFormat.LINE_MAX_HINT) }
                return@launch
            }
            var path = s.receiptRelativePath
            var extraAppend = ""
            var documentsHint: String? = null
            val cleared = s.receiptCleared && pendingBitmap == null
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
                _state.update { it.copy(saving = false, error = "Vérifiez le reste dû (montant).") }
                return@launch
            }
            if (openingCents != null && MoneyFormat.isOverOpeningMax(openingCents)) {
                _state.update { it.copy(saving = false, error = MoneyFormat.OPENING_MAX_HINT) }
                return@launch
            }
            if (openingCents != null && s.recurrence == RecurrenceChoice.Once) {
                _state.update {
                    it.copy(
                        saving = false,
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
            if (s.isAgreement && feeRaw.isNotEmpty() && (feeAdd == null || feeAdd <= 0L)) {
                _state.update { it.copy(saving = false, error = "Vérifiez le montant des frais.") }
                return@launch
            }
            if (feeAdd != null && feeAdd > 0L && MoneyFormat.isOverLineMax(feeAdd)) {
                _state.update { it.copy(saving = false, error = MoneyFormat.LINE_MAX_HINT) }
                return@launch
            }
            if (s.isAgreement && feeRaw.isEmpty() && s.feeNoteText.isNotBlank()) {
                _state.update {
                    it.copy(
                        saving = false,
                        error = "Indiquez le montant des frais avec le motif."
                    )
                }
                return@launch
            }
            val owner = if (s.applyToFollowing || s.isSavingsLine) {
                instanceDao.getExpenseById(s.id)?.templateId ?: s.id
            } else {
                s.id
            }
            pendingBitmap?.let { bmp ->
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
            pendingPdfBytes?.let { pdf ->
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
                        path = ReceiptPathList.encode(
                            buildList {
                                if (path.isNotBlank() && !cleared) add(path)
                                addAll(ReceiptPathList.parse(s.extraReceiptPaths))
                                add(saved.relativePath)
                            }
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
            if (!s.hasTemplate && pendingPdfBytes == null) {
                path = ReceiptPathList.encode(
                    buildList {
                        if (path.isNotBlank() && !cleared) add(path)
                        addAll(ReceiptPathList.parse(s.extraReceiptPaths))
                    }
                )
            }
            val syncCreditor = s.isExpense && (s.isAgreement || s.dossierExpanded)
            when (
                val result = updateInstance(
                    id = s.id,
                    isExpense = s.isExpense,
                    label = s.label,
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
                    feeAddCents = feeAdd ?: 0L,
                    feeNote = s.feeNoteText,
                    extraReceiptAppend = extraAppend,
                    extraReceiptPathsReplace = extrasReplace,
                    keepDoneAmount = s.isSavingsLine
                )
            ) {
                UpdateInstanceResult.Success -> {
                    pendingBitmap = null
                    pendingPdfBytes = null
                    _state.update {
                        it.copy(
                            saving = false,
                            done = true,
                            hasPendingPhoto = false,
                            hasPendingPdf = false,
                            copyMessage = documentsHint
                        )
                    }
                }
                UpdateInstanceResult.NotFound ->
                    _state.update { it.copy(saving = false, error = "Cette ligne n’est plus là.") }
                is UpdateInstanceResult.InvalidInput ->
                    _state.update { it.copy(saving = false, error = result.message) }
            }
        }
    }

    fun delete() {
        if (_state.value.saving || _state.value.done) return
        if (_state.value.futurePreviewReadOnly) {
            _state.update {
                it.copy(
                    error = "Aperçu seulement — on ne supprime pas sur un mois à venir."
                )
            }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
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
                DeleteInstanceResult.FuturePreviewBlocked ->
                    _state.update {
                        it.copy(
                            saving = false,
                            error = "Aperçu seulement — on ne supprime pas sur un mois à venir."
                        )
                    }
            }
        }
    }

    private fun isFuturePreviewLedger(ledgerId: String): Boolean {
        val ym = runCatching { YearMonth.parse(ledgerId) }.getOrNull() ?: return false
        return MonthNavigation.isFuturePreview(ym, AppClock.currentYearMonth())
    }
}
