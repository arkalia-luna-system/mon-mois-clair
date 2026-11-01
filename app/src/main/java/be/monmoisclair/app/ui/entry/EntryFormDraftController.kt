package be.monmoisclair.app.ui.entry

import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.domain.EpcPayment

/**
 * Glue brouillon partagée Add/Edit — mutations pures, sans Hilt.
 * Les VM gardent load/save/delete ; ce controller évite le drift EPC / OCR.
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

    fun ocrBusyMessage(): String = BelgianStructuredCommunication.OCR_BUSY

    fun ocrFound(current: PaymentFields, found: String): PaymentFields =
        current.copy(
            paymentNote = found,
            copyMessage = BelgianStructuredCommunication.OCR_FOUND
        )

    fun ocrNotFound(current: PaymentFields): PaymentFields =
        current.copy(copyMessage = null)
}
