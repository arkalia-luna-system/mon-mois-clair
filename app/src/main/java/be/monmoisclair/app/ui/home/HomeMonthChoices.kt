package be.monmoisclair.app.ui.home

import be.monmoisclair.app.domain.MonthNavigation
import java.time.YearMonth

/**
 * Mois proposés dans le Menu (calendrier compact).
 * Futur proche (aperçu) d’abord, puis mois courant → passé.
 */
object HomeMonthChoices {
    /** 24 mois passés + courant (sans compter le futur). */
    const val DEFAULT_COUNT = MonthNavigation.HISTORY_MONTHS

    /**
     * Liste Menu : M+[FUTURE] … M+1, puis M, M−1, … (horizon passé).
     * Total = FUTURE_MONTHS + HISTORY_MONTHS.
     */
    fun recentMonths(
        current: YearMonth,
        pastCount: Int = DEFAULT_COUNT,
        futureCount: Int = MonthNavigation.FUTURE_MONTHS
    ): List<YearMonth> {
        val future = (futureCount downTo 1).map { current.plusMonths(it.toLong()) }
        val pastAndCurrent = (0 until pastCount).map { current.minusMonths(it.toLong()) }
        return future + pastAndCurrent
    }

    /** Années dans l’ordre d’apparition des mois (récent → ancien). */
    fun yearsInOrder(months: List<YearMonth>): List<Int> =
        months.map { it.year }.distinct()

    fun canSelect(target: YearMonth, current: YearMonth): Boolean =
        MonthNavigation.canSelect(target, current)
}
