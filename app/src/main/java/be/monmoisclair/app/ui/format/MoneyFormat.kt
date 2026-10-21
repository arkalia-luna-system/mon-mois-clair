package be.monmoisclair.app.ui.format

import be.monmoisclair.app.domain.EuroFormat

object MoneyFormat {

    /**
     * Format belge premium : espace fines pour milliers, virgule décimale, € après.
     * Ex. `1 234,50 €` — jamais le symbole avant le montant.
     */
    fun formatCents(cents: Long): String = EuroFormat.formatCents(cents)

    fun formatSignedCents(cents: Long, isExpense: Boolean): String {
        val abs = kotlin.math.abs(cents)
        val body = formatCents(abs)
        return if (isExpense) "\u2212 $body" else "+ $body"
    }

    /**
     * Texte éditable dans un champ (virgule belge, sans € ni espaces milliers).
     * Ex. 47050 → `"470,50"` ; 47000 → `"470"`.
     */
    fun centsToEditable(cents: Long): String {
        val abs = kotlin.math.abs(cents)
        val rem = (abs % 100).toInt()
        return if (rem == 0) (abs / 100).toString() else "%d,%02d".format(abs / 100, rem)
    }

    /**
     * Parse "470", "470,50", "470.5" → centimes.
     * Entiers uniquement (pas de Double) — conforme charte montants.
     */
    fun parseToCents(raw: String): Long? {
        val cleaned = raw.trim()
            .replace("€", "")
            .replace("\u00A0", "")
            .replace("\u202F", "")
            .replace(" ", "")
            .replace(',', '.')
        if (cleaned.isEmpty()) return null
        if (cleaned.count { it == '.' } > 1) return null
        if (cleaned.any { !it.isDigit() && it != '.' }) return null

        val parts = cleaned.split('.')
        val eurosPart = parts[0]
        if (eurosPart.isEmpty() || eurosPart.length > 12) return null
        val euros = eurosPart.toLongOrNull() ?: return null

        val centsPart = when {
            parts.size == 1 -> 0
            parts[1].isEmpty() -> 0
            parts[1].length == 1 -> (parts[1].toIntOrNull() ?: return null) * 10
            parts[1].length == 2 -> parts[1].toIntOrNull() ?: return null
            // E07 : plus de 2 décimales → refuser (pas tronquer en silence).
            else -> return null
        }
        if (centsPart !in 0..99) return null
        return euros * 100 + centsPart
    }

    /** Plus de deux chiffres après la virgule : ne pas couper en silence. */
    fun tooManyFractionDigits(raw: String): Boolean {
        val cleaned = raw.trim()
            .replace("€", "")
            .replace("\u00A0", "")
            .replace("\u202F", "")
            .replace(" ", "")
        val sep = when {
            ',' in cleaned -> ','
            '.' in cleaned -> '.'
            else -> return false
        }
        return cleaned.substringAfter(sep).length > 2
    }

    /**
     * Plafond d’une ligne du mois (facture, courses, mettre de côté, versement).
     * Au-delà = faute de frappe / doigt coincé (ex. 99999), pas un loyer réel.
     */
    const val MAX_LINE_EUROS = 50_000L
    const val MAX_LINE_CENTS = MAX_LINE_EUROS * 100

    /** Reste dû d’un accord : plus haut, mais borné (anti-saisie absurde). */
    const val MAX_OPENING_EUROS = 2_000_000L
    const val MAX_OPENING_CENTS = MAX_OPENING_EUROS * 100

    fun isOverLineMax(cents: Long): Boolean = cents > MAX_LINE_CENTS

    fun isOverOpeningMax(cents: Long): Boolean = cents > MAX_OPENING_CENTS

    const val FRACTION_HINT = "Écrivez le montant avec une virgule, comme 12,50"

    const val LINE_MAX_HINT =
        "Montant trop élevé (max 50 000 €). Vérifiez le chiffre."

    const val OPENING_MAX_HINT =
        "Reste dû trop élevé (max 2 000 000 €). Vérifiez le chiffre."
}
