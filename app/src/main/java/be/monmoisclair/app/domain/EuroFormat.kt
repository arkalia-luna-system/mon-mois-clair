package be.monmoisclair.app.domain

/**
 * Format belge unique (notifications, mail, UI via [be.monmoisclair.app.ui.format.MoneyFormat]) :
 * espace fine pour les milliers, virgule, € après.
 */
object EuroFormat {

    fun formatCents(cents: Long): String {
        val negative = cents < 0
        val abs = kotlin.math.abs(cents)
        val euros = abs / 100
        val rem = (abs % 100).toInt()
        val intPart = formatThousands(euros)
        val body = if (rem == 0) "$intPart €" else "%s,%02d €".format(intPart, rem)
        return if (negative) "\u2212 $body" else body
    }

    private fun formatThousands(value: Long): String {
        val raw = value.toString()
        if (raw.length <= 3) return raw
        val sb = StringBuilder()
        var i = raw.length
        while (i > 0) {
            val start = (i - 3).coerceAtLeast(0)
            if (sb.isNotEmpty()) sb.insert(0, '\u202F')
            sb.insert(0, raw.substring(start, i))
            i = start
        }
        return sb.toString()
    }
}
