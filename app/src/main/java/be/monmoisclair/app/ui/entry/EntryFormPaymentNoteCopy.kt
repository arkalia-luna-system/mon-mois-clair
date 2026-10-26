package be.monmoisclair.app.ui.entry

import android.content.Context
import be.monmoisclair.app.domain.BelgianStructuredCommunication
import be.monmoisclair.app.ui.clipboard.SensitiveClipboard

/** Copie communication +++ — logique unique Add / Edit. */
object EntryFormPaymentNoteCopy {

    data class Result(
        val ok: Boolean,
        val message: String
    )

    fun copy(context: Context, paymentNote: String): Result {
        val prepared = BelgianStructuredCommunication.prepareCopy(paymentNote)
        if (prepared.clipboardText.isEmpty()) {
            return Result(ok = false, message = prepared.userMessage)
        }
        SensitiveClipboard.setPlainText(context, "Communication", prepared.clipboardText)
        return Result(ok = true, message = prepared.userMessage)
    }
}
