package be.monmoisclair.app.domain

import java.time.YearMonth

/**
 * T6 : calcule le prochain `yyyy-MM` pour un mois-calendaire (1–12)
 * à partir de [from] (inclus si même mois).
 */
object RecurrenceAnchor {

    fun nextYearMonth(monthOfYear: Int, from: YearMonth = AppClock.currentYearMonth()): YearMonth {
        val month = monthOfYear.coerceIn(1, 12)
        var candidate = YearMonth.of(from.year, month)
        if (candidate.isBefore(from)) {
            candidate = candidate.plusYears(1)
        }
        return candidate
    }

    /**
     * P3-F01 : édition d’un modèle hors mois.
     * - Mois inchangé → garder l’ancre `yyyy-MM` en base (pas de saut d’année).
     * - Mois changé → avancer depuis l’ancre existante, sans passer avant [now].
     */
    fun resolveTemplateAnchor(
        selectedMonth: Int,
        existingAnchor: YearMonth?,
        now: YearMonth = AppClock.currentYearMonth()
    ): YearMonth {
        val month = selectedMonth.coerceIn(1, 12)
        // Mois inchangé : l’ancre est la phase (janvier 2026 reste janvier 2026),
        // même si elle est passée. La recalculer casserait un trimestre.
        if (existingAnchor != null && existingAnchor.monthValue == month) {
            return existingAnchor
        }
        val start = existingAnchor ?: now
        var candidate = nextYearMonth(month, from = start)
        if (candidate.isBefore(now)) {
            candidate = nextYearMonth(month, from = now)
        }
        return candidate
    }

    fun frenchMonthLabel(monthOfYear: Int): String = when (monthOfYear.coerceIn(1, 12)) {
        1 -> "Janvier"
        2 -> "Février"
        3 -> "Mars"
        4 -> "Avril"
        5 -> "Mai"
        6 -> "Juin"
        7 -> "Juillet"
        8 -> "Août"
        9 -> "Septembre"
        10 -> "Octobre"
        11 -> "Novembre"
        else -> "Décembre"
    }
}
