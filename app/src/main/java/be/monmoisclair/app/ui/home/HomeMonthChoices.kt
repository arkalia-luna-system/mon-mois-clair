package be.monmoisclair.app.ui.home

import be.monmoisclair.app.domain.MonthNavigation
import java.time.YearMonth

/**
 * Mois proposés dans le Menu (sélection calme, pas un mur de pastilles).
 * Futur proche (aperçu) + courant + passé dans l’horizon [MonthNavigation].
 */
object HomeMonthChoices {
    /** 24 mois passés + courant (sans compter le futur). */
    const val DEFAULT_COUNT = MonthNavigation.HISTORY_MONTHS

    /** Bande « Mois récents » : passés proches + courant + aperçus. */
    const val RECENT_PAST = 2

    /**
     * Liste complète sélectionnable : M+[FUTURE] … M+1, puis M, M−1, … (horizon passé).
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

    /**
     * Bande courte pour le Menu : ordre chronologique (ancien → futur).
     * Ex. août, sept, oct (courant), nov, déc, jan.
     */
    fun recentStrip(
        current: YearMonth,
        pastCount: Int = RECENT_PAST,
        futureCount: Int = MonthNavigation.FUTURE_MONTHS
    ): List<YearMonth> {
        val past = (pastCount downTo 1).map { current.minusMonths(it.toLong()) }
        val future = (1..futureCount).map { current.plusMonths(it.toLong()) }
        return past + current + future
    }

    /**
     * Mois hors bande récente, groupés par année (récent → ancien).
     * Sert la section « Autres mois ».
     */
    fun otherMonthsByYear(
        current: YearMonth,
        recent: List<YearMonth> = recentStrip(current)
    ): List<Pair<Int, List<YearMonth>>> {
        val recentSet = recent.toSet()
        val all = recentMonths(current)
        return yearsInOrder(all).mapNotNull { year ->
            val months = all.filter { it.year == year && it !in recentSet }
            if (months.isEmpty()) null else year to months
        }
    }

    /** Années dans l’ordre d’apparition des mois (récent → ancien). */
    fun yearsInOrder(months: List<YearMonth>): List<Int> =
        months.map { it.year }.distinct()

    fun canSelect(target: YearMonth, current: YearMonth): Boolean =
        MonthNavigation.canSelect(target, current)
}
