package be.monmoisclair.app.domain

import java.time.YearMonth

/**
 * Règles de navigation Home entre mois.
 *
 * - Passé : jusqu’à [HISTORY_MONTHS] (calendrier compris) ; le ledger est créé au tap.
 * - Futur : jusqu’à [FUTURE_MONTHS] (factures / revenus matérialisés).
 *   Édition / suppression OK sur le mois visualisé ; pas de FAIT ni d’ajout (ni courses / épargne).
 */
object MonthNavigation {
    /** ~2 ans passés (mois courant inclus). */
    const val HISTORY_MONTHS = 24

    /**
     * ~2 ans futurs sélectionnables (‹ › + calendrier Accueil).
     * [isFuturePreview] = mois après le courant (garde-fous FAIT / ajout).
     */
    const val FUTURE_MONTHS = 24

    /**
     * Horizon « À venir » (liste utile, pas doublon du calendrier).
     * Capacité max aussi dans [be.monmoisclair.app.domain.usecase.ListUpcomingMonthsUseCase].
     */
    const val UPCOMING_PREVIEW_MONTHS = 6

    fun oldestSelectable(current: YearMonth): YearMonth =
        current.minusMonths((HISTORY_MONTHS - 1).toLong())

    fun farthestSelectable(current: YearMonth): YearMonth =
        current.plusMonths(FUTURE_MONTHS.toLong())

    fun canGoPrev(selected: YearMonth, current: YearMonth): Boolean =
        selected.isAfter(oldestSelectable(current))

    fun canGoNext(selected: YearMonth, current: YearMonth): Boolean =
        selected.isBefore(farthestSelectable(current))

    /** Saut calendrier : passé, mois courant, ou futur dans l’horizon. */
    fun canSelect(target: YearMonth, current: YearMonth): Boolean =
        !target.isBefore(oldestSelectable(current)) &&
            !target.isAfter(farthestSelectable(current))

    fun isFuturePreview(selected: YearMonth, current: YearMonth): Boolean =
        selected.isAfter(current)
}
