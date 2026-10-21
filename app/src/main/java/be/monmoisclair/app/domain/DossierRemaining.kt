package be.monmoisclair.app.domain

/**
 * Lot B — reste dû d’un accord.
 * `openingRemainingCents == null` → pas un dossier.
 * Le reste se calcule : départ − somme des montants **FAIT** (centimes réels).
 */
object DossierRemaining {

    fun isDossier(openingRemainingCents: Long?): Boolean = openingRemainingCents != null

    /** Null si ce n’est pas un dossier. */
    fun remainingCents(openingRemainingCents: Long?, paidDoneCents: Long): Long? {
        val opening = openingRemainingCents ?: return null
        return opening - paidDoneCents.coerceAtLeast(0L)
    }

    fun isSettled(openingRemainingCents: Long?, paidDoneCents: Long): Boolean {
        val rem = remainingCents(openingRemainingCents, paidDoneCents) ?: return false
        return rem <= 0L
    }
}
