package be.monmoisclair.app.domain

import java.util.Locale

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
    const val IBAN_COPIED_OK = "IBAN copié — collez dans votre application bancaire."
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

    /** Affichage écran : `+++XXX/XXXX/XXXXX+++` (12 chiffres valides). */
    fun formatDisplay(twelveDigits: String): String? {
        if (!isValidMod97(twelveDigits)) return null
        return "+++${twelveDigits.substring(0, 3)}/" +
            "${twelveDigits.substring(3, 7)}/" +
            "${twelveDigits.substring(7, 12)}+++"
    }

    /**
     * Cherche une communication structurée **valide** dans un texte OCR.
     * Exige la forme `+++` / `***`, ou 12 chiffres isolés **avec** un contexte
     * (réf / communication / OGM…). Jamais de fenêtre glissante sur tous les chiffres
     * (les séparateurs de milliers d’un montant fabriqueraient une fausse ref).
     * Null = rien de fiable à proposer.
     */
    fun findInOcrText(raw: String): String? {
        if (raw.isBlank()) return null
        // Forme +++XXX/XXXX/XXXXX+++ (espaces OCR tolérés).
        val plusForm = Regex(
            """\+{3}\s*(\d{3})\s*/\s*(\d{4})\s*/\s*(\d{5})\s*\+{3}"""
        )
        for (m in plusForm.findAll(raw)) {
            val digits = m.groupValues[1] + m.groupValues[2] + m.groupValues[3]
            formatDisplay(digits)?.let { return it }
        }
        // Variante ***…*** (certains courriers).
        val starForm = Regex(
            """\*{3}\s*(\d{3})\s*/\s*(\d{4})\s*/\s*(\d{5})\s*\*{3}"""
        )
        for (m in starForm.findAll(raw)) {
            val digits = m.groupValues[1] + m.groupValues[2] + m.groupValues[3]
            formatDisplay(digits)?.let { return it }
        }
        if (!hasOcrCommunicationContext(raw)) return null
        // Douze chiffres isolés seulement si le texte parle déjà d’une référence.
        for (m in Regex("""(?<!\d)(\d{12})(?!\d)""").findAll(raw)) {
            formatDisplay(m.groupValues[1])?.let { return it }
        }
        return null
    }

    /** Mots / marques qui indiquent une communication, pas un montant OCR. */
    internal fun hasOcrCommunicationContext(raw: String): Boolean {
        if (raw.contains("+++") || raw.contains("***")) return true
        // Limites de mot : « refusé » ne doit pas ouvrir la porte aux 12 chiffres.
        return OCR_CONTEXT_MARKER.containsMatchIn(raw)
    }

    private val OCR_CONTEXT_MARKER = Regex(
        """(?i)(\br[ée]f\.?\b|\bcommunication\b|\bogm\b|\bvcs\b|\bstructured\b|\bbancaire\b|\bvirement\b)"""
    )

    /** Exemple canonique du plan : base 0 → contrôle 97. */
    const val EXAMPLE_VALID_DISPLAY = "+++000/0000/00097+++"

    const val OCR_FOUND = "Communication lue sur la photo — vérifiez avant d’enregistrer."
    const val OCR_NOT_FOUND =
        "Aucune communication +++ valide sur cette photo. Saisissez-la ou scannez le QR."
    const val OCR_NO_PHOTO = "Prenez d’abord une photo de la facture."
    const val OCR_BUSY = "Lecture de la photo…"

    /**
     * Presse-papiers banque : lettres et chiffres, majuscules.
     * Un préfixe « IBAN » collé devant (souvent sur le courrier) est retiré.
     * L’écran garde la saisie.
     */
    fun ibanForClipboard(raw: String): String {
        val compact = raw.filter { it.isLetterOrDigit() }.uppercase(Locale.ROOT)
        if (compact.startsWith("IBAN") && compact.length > 6) {
            val rest = compact.removePrefix("IBAN")
            if (ibanLooksCopyable(rest)) return rest
        }
        return compact
    }

    /** 2 lettres de pays, 2 chiffres de contrôle, longueur ISO (15 à 34). */
    fun ibanLooksCopyable(compact: String): Boolean =
        compact.length in 15..34 &&
            compact[0].isLetter() &&
            compact[1].isLetter() &&
            compact[2].isDigit() &&
            compact[3].isDigit()

    /**
     * Contrôle ISO 13616 : après déplacement des 4 premiers caractères,
     * lettres A=10…Z=35, le nombre modulo 97 vaut 1.
     */
    fun ibanChecksumOk(compact: String): Boolean {
        if (!ibanLooksCopyable(compact)) return false
        val rearranged = compact.substring(4) + compact.substring(0, 4)
        var remainder = 0
        for (ch in rearranged) {
            val chunk = when {
                ch.isDigit() -> ch.toString()
                ch.isLetter() -> (ch.code - 'A'.code + 10).toString()
                else -> return false
            }
            for (digit in chunk) {
                remainder = (remainder * 10 + (digit.code - '0'.code)) % 97
            }
        }
        return remainder == 1
    }
}
