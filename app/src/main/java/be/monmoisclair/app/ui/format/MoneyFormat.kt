package be.monmoisclair.app.ui.format

object MoneyFormat {

    /**
     * Format belge premium : espace fines pour milliers, virgule décimale, € après.
     * Ex. `1 234,50 €` — jamais le symbole avant le montant.
     */
    fun formatCents(cents: Long): String {
        val negative = cents < 0
        val abs = kotlin.math.abs(cents)
        val euros = abs / 100
        val rem = (abs % 100).toInt()
        val intPart = formatThousands(euros)
        val body = if (rem == 0) "$intPart €" else "%s,%02d €".format(intPart, rem)
        return if (negative) "\u2212 $body" else body
    }

    fun formatSignedCents(cents: Long, isExpense: Boolean): String {
        val abs = kotlin.math.abs(cents)
        val body = formatCents(abs)
        return if (isExpense) "\u2212 $body" else "+ $body"
    }

    private fun formatThousands(value: Long): String {
        val raw = value.toString()
        if (raw.length <= 3) return raw
        val sb = StringBuilder()
        var i = raw.length
        while (i > 0) {
            val start = (i - 3).coerceAtLeast(0)
            if (sb.isNotEmpty()) sb.insert(0, '\u202F') // espace fine insécable
            sb.insert(0, raw.substring(start, i))
            i = start
        }
        return sb.toString()
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
            else -> {
                parts[1].take(2).toIntOrNull() ?: return null
            }
        }
        if (centsPart !in 0..99) return null
        return euros * 100 + centsPart
    }
}
