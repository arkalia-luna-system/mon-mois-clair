package be.monmoisclair.app.domain

/**
 * Lot E — effet d’une ligne « Mettre de côté » sur le Reste du mois.
 * [oldTodoCents] est le montant déjà compté et encore à payer (0 si la ligne
 * n’existe pas, ou si elle est déjà FAIT : ce virement ne bouge plus).
 */
object SavingsProjection {

    fun remainingAfter(
        currentRemainingCents: Long,
        oldTodoCents: Long,
        newCents: Long
    ): Long = currentRemainingCents + oldTodoCents.coerceAtLeast(0L) - newCents
}
