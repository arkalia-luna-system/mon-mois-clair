package be.monmoisclair.app.ui.entry

import be.monmoisclair.app.domain.EpcPayment
import be.monmoisclair.app.domain.EpcQrParser
import be.monmoisclair.app.ui.format.MoneyFormat

/**
 * Mapping QR EPC → champs fiche (Add + Edit). Pur, testable JVM.
 */
object EntryFormEpcApply {

    data class Patch(
        val label: String,
        val amountText: String,
        val paymentNote: String,
        val iban: String,
        val copyMessage: String?
    )

    fun parseOrNull(raw: String): EpcPayment? = EpcQrParser.parse(raw)

    fun patch(
        payment: EpcPayment,
        currentLabel: String,
        currentAmountText: String,
        currentPaymentNote: String,
        currentIban: String,
        currentCopyMessage: String?,
        openingRemainingText: String,
        dossierExpanded: Boolean
    ): Patch {
        val amountText = payment.amountCents?.let { MoneyFormat.centsToEditable(it) }
            ?: currentAmountText
        val keepIbanHint = openingRemainingText.isBlank() &&
            payment.iban.isNotBlank() &&
            !dossierExpanded
        return Patch(
            label = payment.beneficiaryName.ifBlank { currentLabel },
            amountText = amountText,
            paymentNote = payment.remittance.ifBlank { currentPaymentNote },
            iban = payment.iban.ifBlank { currentIban },
            copyMessage = if (keepIbanHint) {
                EntryFormMessages.EPC_IBAN_HINT
            } else {
                currentCopyMessage
            }
        )
    }
}
