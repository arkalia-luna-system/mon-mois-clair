package be.monmoisclair.app.domain

/**
 * Retour famille → Arkalia Système (aucun serveur : intent Mail / Gmail).
 * Pièce jointe (capture) : l’utilisatrice l’ajoute dans Gmail après ouverture.
 */
object SupportMailComposer {

    const val SUPPORT_EMAIL = "arkalia.luna.system@gmail.com"
    const val SUPPORT_DISPLAY_NAME = "Arkalia Système"

    data class Draft(
        val to: String = SUPPORT_EMAIL,
        val subject: String,
        val body: String
    )

    fun build(
        versionName: String,
        versionCode: Int,
        deviceModel: String,
        monthLabel: String? = null
    ): Draft {
        val subject = "Mon Mois Clair — retour ($versionName)"
        val body = buildString {
            appendLine("Bonjour $SUPPORT_DISPLAY_NAME,")
            appendLine()
            appendLine("Voici ce qui ne va pas (texte ou capture jointe dans Mail) :")
            appendLine()
            appendLine()
            appendLine()
            appendLine("—")
            appendLine("Version : $versionName ($versionCode)")
            if (!monthLabel.isNullOrBlank()) appendLine("Mois affiché : $monthLabel")
            appendLine("Téléphone : ${deviceModel.trim().ifBlank { "—" }}")
        }
        return Draft(subject = subject, body = body)
    }

    fun mailtoUri(draft: Draft): String = CreditorMailComposer.mailtoUri(
        CreditorMailComposer.Draft(
            to = draft.to,
            subject = draft.subject,
            body = draft.body
        )
    )
}
