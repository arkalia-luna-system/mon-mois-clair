package be.monmoisclair.app.domain

import android.content.Intent
import android.net.Uri

/**
 * Intents support « Un souci » — SENDTO mailto d’abord, SEND text/plain en secours.
 * Messages FR testables hors Compose.
 */
object SupportMailIntents {

    const val NO_MAIL_APP =
        "Aucune app Mail trouvée. Copiez l’adresse ou réessayez."
    const val ADDRESS_COPIED = "Adresse copiée — collez-la dans Mail."

    fun sendToChooser(draft: SupportMailComposer.Draft, chooserTitle: String): Intent {
        val sendTo = Intent(
            Intent.ACTION_SENDTO,
            Uri.parse(SupportMailComposer.mailtoUri(draft))
        )
        return Intent.createChooser(sendTo, chooserTitle)
    }

    /** Fallback OEM / package-visibility : corps + destinataire en EXTRA. */
    fun sendFallbackChooser(draft: SupportMailComposer.Draft, chooserTitle: String): Intent {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(draft.to))
            putExtra(Intent.EXTRA_SUBJECT, draft.subject)
            putExtra(Intent.EXTRA_TEXT, draft.body)
        }
        return Intent.createChooser(send, chooserTitle)
    }

    fun chooserTitle(): String =
        "Écrire à ${SupportMailComposer.SUPPORT_DISPLAY_NAME}"
}
