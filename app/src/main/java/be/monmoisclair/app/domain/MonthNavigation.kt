package be.monmoisclair.app.domain

import java.time.YearMonth

/**
 * Règles de navigation Home entre mois.
 *
 * - Passé : toujours autorisé ; le ledger est créé au tap (lazy), pas au cold start.
 * - Futur : jamais (pas de mois fantôme après le mois calendaire courant).
 */
object MonthNavigation {
    fun canGoPrev(): Boolean = true

    fun canGoNext(selected: YearMonth, current: YearMonth): Boolean = selected < current

    /** Saut calendrier Menu : passé ou mois courant, jamais le futur. */
    fun canSelect(target: YearMonth, current: YearMonth): Boolean = !target.isAfter(current)
}
