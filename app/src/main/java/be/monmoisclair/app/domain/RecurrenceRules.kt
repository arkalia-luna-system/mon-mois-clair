package be.monmoisclair.app.domain

import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Règle I : matérialiser une instance au mois [target] ssi
 * target >= anchor et monthsBetween % intervalMonths == 0.
 */
object RecurrenceRules {
    fun shouldMaterialize(
        anchorYearMonth: YearMonth,
        intervalMonths: Int,
        target: YearMonth
    ): Boolean {
        val interval = intervalMonths.coerceAtLeast(1)
        if (target.isBefore(anchorYearMonth)) return false
        val between = ChronoUnit.MONTHS.between(anchorYearMonth, target).toInt()
        return between % interval == 0
    }

    fun parseAnchorOrNull(raw: String): YearMonth? =
        raw.trim().takeIf { it.isNotEmpty() }?.let { runCatching { YearMonth.parse(it) }.getOrNull() }
}
