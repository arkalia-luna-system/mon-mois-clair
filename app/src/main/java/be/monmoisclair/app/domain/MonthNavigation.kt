package be.monmoisclair.app.domain

import java.time.YearMonth

/**
 * Règles de navigation Home entre mois.
 *
 * - Passé : jusqu’à 24 mois (menu compris) ; le ledger est créé au tap, pas au cold start.
 * - Futur : jamais (pas de mois fantôme après le mois calendaire courant).
 */
object MonthNavigation {
    /** Même horizon que le menu : mois courant inclus. */
    const val HISTORY_MONTHS = 24

    fun oldestSelectable(current: YearMonth): YearMonth =
        current.minusMonths((HISTORY_MONTHS - 1).toLong())

    fun canGoPrev(selected: YearMonth, current: YearMonth): Boolean =
        selected.isAfter(oldestSelectable(current))

    fun canGoNext(selected: YearMonth, current: YearMonth): Boolean = selected < current

    /** Saut calendrier Menu : passé dans l’horizon, ou mois courant. Jamais le futur. */
    fun canSelect(target: YearMonth, current: YearMonth): Boolean =
        !target.isAfter(current) && !target.isBefore(oldestSelectable(current))
}
