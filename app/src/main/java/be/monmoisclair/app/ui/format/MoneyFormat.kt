package be.monmoisclair.app.ui.format

import java.text.NumberFormat
import java.util.Locale

object MoneyFormat {
    private val frBe: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("fr-BE"))

    fun formatCents(cents: Long): String {
        // Affichage uniquement (NumberFormat) — les calculs métier restent en Long.
        return frBe.format(cents / 100.0)
    }

    fun formatSignedCents(cents: Long, isExpense: Boolean): String {
        val abs = kotlin.math.abs(cents)
        val euros = abs / 100
        val rem = (abs % 100).toInt()
        val body = if (rem == 0) "$euros €" else "%d,%02d €".format(euros, rem)
        return if (isExpense) "- $body" else "+ $body"
    }

    /**
     * Parse "470", "470,50", "470.5" → centimes.
     * Entiers uniquement (pas de Double) — conforme charte montants.
     */
    fun parseToCents(raw: String): Long? {
        val cleaned = raw.trim()
            .replace("€", "")
            .replace("\u00A0", "")
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
            else -> {
                // Tronque à 2 décimales (saisie clavier)
                parts[1].take(2).toIntOrNull() ?: return null
            }
        }
        if (centsPart !in 0..99) return null
        return euros * 100 + centsPart
    }
}
