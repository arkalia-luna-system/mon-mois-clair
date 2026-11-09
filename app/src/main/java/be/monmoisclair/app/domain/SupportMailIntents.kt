package be.monmoisclair.app.domain

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * Intents support « Un souci » — SENDTO mailto d’abord, SEND rfc822 en secours.
 * Messages FR testables hors Compose.
 *
 * Important : [Intent.createChooser] masque [android.content.ActivityNotFoundException].
 * Toujours [canResolveSendTo] avant de lancer un chooser SENDTO.
 */
object SupportMailIntents {

    const val NO_MAIL_APP =
        "Aucune app Mail trouvée. Copiez l’adresse ou réessayez."
    const val ADDRESS_COPIED = "Adresse copiée — collez-la dans Mail."

    fun sendToIntent(mailtoUri: String): Intent =
        Intent(Intent.ACTION_SENDTO, Uri.parse(mailtoUri))

    fun sendToChooser(draft: SupportMailComposer.Draft, chooserTitle: String): Intent {
        val sendTo = sendToIntent(SupportMailComposer.mailtoUri(draft))
        return Intent.createChooser(sendTo, chooserTitle)
    }

    fun sendToChooser(mailtoUri: String, chooserTitle: String): Intent =
        Intent.createChooser(sendToIntent(mailtoUri), chooserTitle)

    /** Fallback OEM / package-visibility : corps + destinataire en EXTRA. */
    fun sendFallbackChooser(
        to: String,
        subject: String,
        body: String,
        chooserTitle: String
    ): Intent {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        return Intent.createChooser(send, chooserTitle)
    }

    fun sendFallbackChooser(draft: SupportMailComposer.Draft, chooserTitle: String): Intent =
        sendFallbackChooser(draft.to, draft.subject, draft.body, chooserTitle)

    fun sendFallbackChooser(
        draft: CreditorMailComposer.Draft,
        chooserTitle: String
    ): Intent = sendFallbackChooser(draft.to, draft.subject, draft.body, chooserTitle)

    /**
     * true si au moins une activité gère SENDTO mailto
     * ([PackageManager.resolveActivity] ou query).
     */
    fun canResolveSendTo(packageManager: PackageManager, mailtoUri: String): Boolean {
        val intent = sendToIntent(mailtoUri)
        if (intent.resolveActivity(packageManager) != null) return true
        @Suppress("DEPRECATION")
        val listed = packageManager.queryIntentActivities(intent, 0)
        return listed.isNotEmpty()
    }

    fun canResolveSendTo(
        packageManager: PackageManager,
        draft: SupportMailComposer.Draft
    ): Boolean = canResolveSendTo(packageManager, SupportMailComposer.mailtoUri(draft))

    fun canResolveSendTo(
        packageManager: PackageManager,
        draft: CreditorMailComposer.Draft
    ): Boolean = canResolveSendTo(packageManager, CreditorMailComposer.mailtoUri(draft))

    fun chooserTitle(): String =
        "Écrire à ${SupportMailComposer.SUPPORT_DISPLAY_NAME}"

    const val CREDITOR_CHOOSER_TITLE = "Écrire"
}
