package be.monmoisclair.app.ui.entry

import android.content.Context
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.ui.clipboard.SensitiveClipboard

/** Copier IBAN créancier — partagé Add / Edit. */
object EntryFormCreditorCopy {

    data class Outcome(
        val copyMessage: String? = null,
        val error: String? = null
    )

    fun copyIban(context: Context, rawIban: String): Outcome {
        if (rawIban.isBlank()) {
            return Outcome(error = "Aucun IBAN à copier.")
        }
        val compact = BelgianStructuredCommunication.ibanForClipboard(rawIban)
        if (compact.isBlank() || compact == "IBAN") {
            return Outcome(error = "Aucun IBAN à copier.")
        }
        if (!BelgianStructuredCommunication.ibanChecksumOk(compact)) {
            return Outcome(error = "Vérifiez l’IBAN sur le courrier.")
        }
        SensitiveClipboard.setPlainText(context, "IBAN", compact)
        return Outcome(copyMessage = BelgianStructuredCommunication.IBAN_COPIED_OK)
    }
}
