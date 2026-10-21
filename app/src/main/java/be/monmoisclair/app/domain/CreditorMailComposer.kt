package be.monmoisclair.app.domain

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
            add("Montant de ce mois : ${formatEuros(monthAmountCents)}")
            if (paymentNote.isNotBlank()) add("Référence : ${paymentNote.trim()}")
            add("")
            add("Cordialement")
        }
        return Draft(to = to, subject = subject, body = lines.joinToString("\n"))
    }

    private fun formatEuros(cents: Long): String {
        val euros = cents / 100
        val rem = (cents % 100).toInt()
        return if (rem == 0) "$euros €" else "%d,%02d €".format(euros, rem)
    }
}
