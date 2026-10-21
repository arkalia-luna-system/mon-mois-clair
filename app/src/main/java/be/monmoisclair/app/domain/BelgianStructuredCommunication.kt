package be.monmoisclair.app.domain

/**
 * Communication structurée belge (OGM / VCS) : 12 chiffres, les 2 derniers = modulo 97
 * des 10 premiers (reste 0 → 97). Affichage courant : `+++XXX/XXXX/XXXXX+++`.
 *
 * Lot A : à la copie, si valide → presse-papiers = 12 chiffres (ce que la banque accepte).
 */
object BelgianStructuredCommunication {

    data class CopyResult(
        /** Texte réellement mis dans le presse-papiers. */
        val clipboardText: String,
        /**
         * Message utilisateur.
         * Structurée invalide → « Vérifiez sur le courrier. »
         * Sinon → confirmation de copie.
         */
        val userMessage: String
    )

    const val COPIED_OK = "Copié — collez dans votre application bancaire."
    const val VERIFY_ON_MAIL = "Vérifiez sur le courrier."

    fun prepareCopy(raw: String): CopyResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return CopyResult("", "Aucune communication à copier.")
        }
        val digits = trimmed.filter { it.isDigit() }
        val structural = trimmed.all {
            it.isDigit() || it == '+' || it == '/' || it == '*' || it.isWhitespace()
        }
        if (structural && digits.length == 12 && isValidMod97(digits)) {
            return CopyResult(clipboardText = digits, userMessage = COPIED_OK)
        }
        val looksStructured = trimmed.contains("+++") || trimmed.contains("***") ||
            (structural && digits.length == 12)
        if (looksStructured) {
            return CopyResult(clipboardText = trimmed, userMessage = VERIFY_ON_MAIL)
        }
        return CopyResult(clipboardText = trimmed, userMessage = COPIED_OK)
    }

    fun isValidMod97(twelveDigits: String): Boolean {
        if (twelveDigits.length != 12 || twelveDigits.any { !it.isDigit() }) return false
        val base = twelveDigits.substring(0, 10).toLong()
        val check = twelveDigits.substring(10, 12).toInt()
        val rem = (base % 97L).toInt()
        val expected = if (rem == 0) 97 else rem
        return check == expected
    }

    /** Exemple canonique du plan : base 0 → contrôle 97. */
    const val EXAMPLE_VALID_DISPLAY = "+++000/0000/00097+++"
}
