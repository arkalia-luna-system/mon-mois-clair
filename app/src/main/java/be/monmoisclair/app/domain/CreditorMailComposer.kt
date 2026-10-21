package be.monmoisclair.app.domain

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Lot C — texte pour l’app Mail (aucun fil stocké dans Mon Mois Clair).
 */
object CreditorMailComposer {

    data class Draft(
        val to: String,
        val subject: String,
        val body: String
    )

    fun build(
        email: String,
        who: String,
        what: String,
        monthAmountCents: Long,
        paymentNote: String
    ): Draft {
        val to = email.trim()
        val subject = paymentNote.trim().ifBlank { who.trim().ifBlank { "Versement" } }
        val lines = buildList {
            add("Bonjour,")
            add("")
            if (who.isNotBlank()) add("Concernant : $who")
            if (what.isNotBlank()) add("Pour : $what")
            add("Montant de ce mois : ${EuroFormat.formatCents(monthAmountCents)}")
            if (paymentNote.isNotBlank()) add("Référence : ${paymentNote.trim()}")
            add("")
            add("Cordialement")
        }
        return Draft(to = to, subject = subject, body = lines.joinToString("\n"))
    }

    /**
     * URI `mailto:` complète (destinataire + subject + body).
     * Ne pas passer par [android.net.Uri.Builder] sur un mailto opaque :
     * Gmail et d’autres clients ignorent souvent les extras SENDTO.
     */
    fun mailtoUri(draft: Draft): String {
        fun q(value: String): String =
            URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
        val to = draft.to.trim()
        return "mailto:$to?subject=${q(draft.subject)}&body=${q(draft.body)}"
    }
}
