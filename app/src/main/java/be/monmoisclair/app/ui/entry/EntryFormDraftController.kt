package be.monmoisclair.app.ui.entry

import android.graphics.Bitmap
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.domain.EpcPayment

/**
 * Glue brouillon partagée Add/Edit — mutations pures (+ draft mémoire), sans Hilt.
 * Les VM gardent I/O load/save/delete ; ce controller évite le drift EPC / OCR / dossier /
 * drapeaux photo-PDF.
 */
object EntryFormDraftController {

    data class PaymentFields(
        val label: String,
        val amountText: String,
        val paymentNote: String,
        val iban: String,
        val copyMessage: String?,
        val openingRemainingText: String,
        val dossierExpanded: Boolean
    )

    data class EpcResult(
        val fields: PaymentFields,
        /** Add force facture ; Edit conserve le type courant. */
        val forceExpense: Boolean = true
    )

    /** Patch champs paiement après EPC (commun Add/Edit). */
    data class PaymentPatch(
        val label: String,
        val amountText: String,
        val paymentNote: String,
        val iban: String,
        val copyMessage: String?
    )

    sealed class OcrOutcome {
        data class Applied(val fields: PaymentFields) : OcrOutcome()
        data class NotFound(val fields: PaymentFields) : OcrOutcome()
    }

    /** Résultat EPC prêt à coller dans l’état VM (Add forceExpense ; Edit ignore). */
    sealed class EpcOutcome {
        data class Ok(val patch: PaymentPatch, val forceExpense: Boolean) : EpcOutcome()
        data object Invalid : EpcOutcome()
    }

    fun paymentFields(
        label: String,
        amountText: String,
        paymentNote: String,
        iban: String,
        copyMessage: String?,
        openingRemainingText: String,
        dossierExpanded: Boolean
    ): PaymentFields = PaymentFields(
        label = label,
        amountText = amountText,
        paymentNote = paymentNote,
        iban = iban,
        copyMessage = copyMessage,
        openingRemainingText = openingRemainingText,
        dossierExpanded = dossierExpanded
    )

    fun applyEpc(current: PaymentFields, payment: EpcPayment): EpcResult {
        val patch = EntryFormEpcApply.patch(
            payment = payment,
            currentLabel = current.label,
            currentAmountText = current.amountText,
            currentPaymentNote = current.paymentNote,
            currentIban = current.iban,
            currentCopyMessage = current.copyMessage,
            openingRemainingText = current.openingRemainingText,
            dossierExpanded = current.dossierExpanded
        )
        return EpcResult(
            fields = current.copy(
                label = patch.label,
                amountText = patch.amountText,
                paymentNote = patch.paymentNote,
                iban = patch.iban,
                copyMessage = patch.copyMessage
            ),
            forceExpense = true
        )
    }

    fun applyEpcRawOrNull(current: PaymentFields, raw: String): EpcResult? {
        val payment = EntryFormEpcApply.parseOrNull(raw) ?: return null
        return applyEpc(current, payment)
    }

    fun paymentPatch(result: EpcResult): PaymentPatch = PaymentPatch(
        label = result.fields.label,
        amountText = result.fields.amountText,
        paymentNote = result.fields.paymentNote,
        iban = result.fields.iban,
        copyMessage = result.fields.copyMessage
    )

    fun epcOutcomeFromRaw(current: PaymentFields, raw: String): EpcOutcome {
        val result = applyEpcRawOrNull(current, raw) ?: return EpcOutcome.Invalid
        return EpcOutcome.Ok(paymentPatch(result), forceExpense = result.forceExpense)
    }

    fun epcOutcomeFromPayment(current: PaymentFields, payment: EpcPayment): EpcOutcome {
        val result = applyEpc(current, payment)
        return EpcOutcome.Ok(paymentPatch(result), forceExpense = result.forceExpense)
    }

    fun ocrBusyMessage(): String = BelgianStructuredCommunication.OCR_BUSY

    fun ocrFound(current: PaymentFields, found: String): PaymentFields =
        current.copy(
            paymentNote = found,
            copyMessage = BelgianStructuredCommunication.OCR_FOUND
        )

    fun ocrNotFound(current: PaymentFields): PaymentFields =
        current.copy(copyMessage = null)

    fun resolveOcr(current: PaymentFields, found: String?): OcrOutcome =
        if (found.isNullOrBlank()) {
            OcrOutcome.NotFound(ocrNotFound(current))
        } else {
            OcrOutcome.Applied(ocrFound(current, found))
        }

    /**
     * Collapse conversion dossier (pas encore accord) : wipe cause / reste.
     * @return Triple(expanded, cause, opening) — cause/opening vidés si collapse.
     */
    fun collapseConversionDossier(
        expanded: Boolean,
        causeLabel: String,
        openingRemainingText: String
    ): Triple<Boolean, String, String> =
        if (!expanded) {
            Triple(false, "", "")
        } else {
            Triple(true, causeLabel, openingRemainingText)
        }

    /** Drapeaux UI pièces jointes (Add/Edit). */
    data class PendingFlags(
        val hasPendingPhoto: Boolean,
        val hasPendingPdf: Boolean
    )

    /** Edit : enlever la photo déjà liée + brouillon. */
    data class EditPhotoClear(
        val flags: PendingFlags,
        val receiptRelativePath: String,
        val receiptCleared: Boolean
    )

    fun peekFlags(draft: EntryFormReceiptDraft): PendingFlags =
        PendingFlags(hasPendingPhoto = draft.hasPhoto, hasPendingPdf = draft.hasPdf)

    fun attachBitmap(draft: EntryFormReceiptDraft, bitmap: Bitmap): PendingFlags {
        draft.attachBitmap(bitmap)
        return PendingFlags(hasPendingPhoto = true, hasPendingPdf = draft.hasPdf)
    }

    fun attachPdf(draft: EntryFormReceiptDraft, bytes: ByteArray): PendingFlags {
        draft.attachPdf(bytes)
        return PendingFlags(hasPendingPhoto = draft.hasPhoto, hasPendingPdf = true)
    }

    fun clearPdf(draft: EntryFormReceiptDraft): PendingFlags {
        draft.clearPdf()
        return PendingFlags(hasPendingPhoto = draft.hasPhoto, hasPendingPdf = false)
    }

    fun clearBitmap(draft: EntryFormReceiptDraft): PendingFlags {
        draft.clearBitmap()
        return PendingFlags(hasPendingPhoto = false, hasPendingPdf = draft.hasPdf)
    }

    fun clearAllAttachments(draft: EntryFormReceiptDraft): PendingFlags {
        draft.clearAll()
        return PendingFlags(hasPendingPhoto = false, hasPendingPdf = false)
    }

    /** Edit — « Enlever la photo » (chemin + flag cleared). */
    fun clearEditReceiptPhoto(draft: EntryFormReceiptDraft): EditPhotoClear {
        val flags = clearBitmap(draft)
        return EditPhotoClear(
            flags = flags,
            receiptRelativePath = "",
            receiptCleared = true
        )
    }

    fun photoDecodeError(): String = EntryFormMessages.PHOTO_DECODE_ERROR

    fun pdfReadError(message: String = EntryFormMessages.PDF_READ_ERROR_DEFAULT): String =
        message

    /**
     * Add : bascule revenu → wipe pièces ; facture → conserve.
     * @return Pair(isExpense, flags)
     */
    fun applyExpenseType(
        draft: EntryFormReceiptDraft,
        isExpense: Boolean
    ): PendingFlags =
        if (isExpense) {
            peekFlags(draft)
        } else {
            clearAllAttachments(draft)
        }
}
