package be.monmoisclair.app.domain

import java.time.YearMonth

/**
 * Règles de navigation Home entre mois.
 *
 * - Passé : jusqu’à 24 mois (menu compris) ; le ledger est créé au tap, pas au cold start.
 * - Futur : jusqu’à [FUTURE_MONTHS] en **aperçu** (factures / revenus matérialisés,
 *   sans FAIT ni ajout — le mois calendaire reste la source de vérité).
 */
object MonthNavigation {
    /** Même horizon que le menu : mois courant inclus. */
    const val HISTORY_MONTHS = 24

    /** Mois futurs sélectionnables (Menu + flèche ›). */
    const val FUTURE_MONTHS = 3

    fun oldestSelectable(current: YearMonth): YearMonth =
        current.minusMonths((HISTORY_MONTHS - 1).toLong())

    fun farthestSelectable(current: YearMonth): YearMonth =
        current.plusMonths(FUTURE_MONTHS.toLong())

    fun canGoPrev(selected: YearMonth, current: YearMonth): Boolean =
        selected.isAfter(oldestSelectable(current))

    fun canGoNext(selected: YearMonth, current: YearMonth): Boolean =
        selected.isBefore(farthestSelectable(current))

    /** Saut calendrier Menu : passé, mois courant, ou futur dans l’horizon. */
    fun canSelect(target: YearMonth, current: YearMonth): Boolean =
        !target.isBefore(oldestSelectable(current)) &&
            !target.isAfter(farthestSelectable(current))

    fun isFuturePreview(selected: YearMonth, current: YearMonth): Boolean =
        selected.isAfter(current)
}
