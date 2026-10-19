package be.monmoisclair.app.ui.format

import java.text.NumberFormat
import java.util.Locale

object MoneyFormat {
    private val frBe: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("fr-BE"))

    fun formatCents(cents: Long, withSign: Boolean = false): String {
        val value = cents / 100.0
        val formatted = frBe.format(value)
        return when {
            !withSign -> formatted
            cents > 0 -> "+ $formatted"
            cents < 0 -> formatted // déjà avec signe
            else -> formatted
        }
    }

    fun formatSignedCents(cents: Long, isExpense: Boolean): String {
        val abs = kotlin.math.abs(cents)
        val euros = abs / 100
        val rem = (abs % 100).toInt()
        val body = if (rem == 0) "$euros €" else "%d,%02d €".format(euros, rem)
        return if (isExpense) "- $body" else "+ $body"
    }

    /** Parse "470" ou "470,50" → centimes. null si invalide. */
    fun parseToCents(raw: String): Long? {
        val cleaned = raw.trim()
            .replace("€", "")
            .replace(" ", "")
            .replace('\u00A0', ' ')
            .trim()
            .replace(',', '.')
        if (cleaned.isEmpty()) return null
        val value = cleaned.toDoubleOrNull() ?: return null
        if (value < 0) return null
        return Math.round(value * 100.0)
    }
}
