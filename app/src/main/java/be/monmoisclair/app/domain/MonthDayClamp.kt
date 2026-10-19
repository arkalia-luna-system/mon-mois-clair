package be.monmoisclair.app.domain

import java.time.YearMonth

/**
 * Borne un jour de template (1..31) au dernier jour réel du mois cible.
 * Ex. dayOfMonth=31 en février non bissextile → 28.
 */
object MonthDayClamp {

    fun clamp(dayOfMonth: Int, yearMonth: YearMonth): Int {
        require(dayOfMonth in 1..31) {
            "dayOfMonth must be in 1..31, was $dayOfMonth"
        }
        return minOf(dayOfMonth, yearMonth.lengthOfMonth())
    }
}
