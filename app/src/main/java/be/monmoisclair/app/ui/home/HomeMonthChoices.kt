package be.monmoisclair.app.ui.home

import be.monmoisclair.app.domain.MonthNavigation
import java.time.YearMonth

/**
 * Mois proposés dans le Menu (calendrier compact).
 * Du mois calendaire courant vers le passé — jamais le futur.
 */
object HomeMonthChoices {
    /** 24 mois : navigation année sans grille trop dense (chips courts). */
    const val DEFAULT_COUNT = 24

    fun recentMonths(
        current: YearMonth,
        count: Int = DEFAULT_COUNT
    ): List<YearMonth> =
        (0 until count).map { current.minusMonths(it.toLong()) }

    fun canSelect(target: YearMonth, current: YearMonth): Boolean =
        MonthNavigation.canSelect(target, current)
}
