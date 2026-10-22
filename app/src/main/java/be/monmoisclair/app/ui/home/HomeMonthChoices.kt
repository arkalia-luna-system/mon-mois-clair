package be.monmoisclair.app.ui.home

import be.monmoisclair.app.domain.MonthNavigation
import java.time.YearMonth

/**
 * Mois proposés au calendrier Accueil (filets + pastilles, pas grille 31 jours).
 * Horizon = [MonthNavigation] (~2 ans passé + futur).
 */
object HomeMonthChoices {
    /** 24 mois passés + courant (sans compter le futur). */
    const val DEFAULT_COUNT = MonthNavigation.HISTORY_MONTHS

    /** Bande courte éventuelle : passés proches (hors calendrier plein). */
    const val RECENT_PAST = 2

    /** Futur proche dans une bande courte — pas tout [MonthNavigation.FUTURE_MONTHS]. */
    const val RECENT_STRIP_FUTURE = 3

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
     * Bande courte chronologique (ancien → futur), pour tests / usages légers.
     * Le Menu n’affiche plus cette bande : calendrier dédié.
     */
    fun recentStrip(
        current: YearMonth,
        pastCount: Int = RECENT_PAST,
        futureCount: Int = RECENT_STRIP_FUTURE
    ): List<YearMonth> {
        val past = (pastCount downTo 1).map { current.minusMonths(it.toLong()) }
        val future = (1..futureCount).map { current.plusMonths(it.toLong()) }
        return past + current + future
    }

    /**
     * Mois hors bande récente, groupés par année (récent → ancien).
     */
    fun otherMonthsByYear(
        current: YearMonth,
        recent: List<YearMonth> = recentStrip(current)
    ): List<Pair<Int, List<YearMonth>>> {
        val recentSet = recent.toSet()
        val all = recentMonths(current)
        return yearsInOrder(all).mapNotNull { year ->
            val months = all.filter { it.year == year && it !in recentSet }
            if (months.isEmpty()) null else year to months.sorted()
        }
    }

    /**
     * Calendrier « Choisir un mois » : toutes les années de l’horizon,
     * mois Jan→Déc dans chaque année, années du plus récent au plus ancien.
     */
    fun calendarByYear(current: YearMonth): List<Pair<Int, List<YearMonth>>> {
        val all = recentMonths(current)
        return yearsInOrder(all).map { year ->
            year to all.filter { it.year == year }.sorted()
        }
    }

    /** Années dans l’ordre d’apparition des mois (récent → ancien). */
    fun yearsInOrder(months: List<YearMonth>): List<Int> =
        months.map { it.year }.distinct()

    fun canSelect(target: YearMonth, current: YearMonth): Boolean =
        MonthNavigation.canSelect(target, current)
}
