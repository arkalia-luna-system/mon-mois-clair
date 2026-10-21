package be.monmoisclair.app.domain

/**
 * Lot B/C — reste dû d’un accord.
 * `openingRemainingCents == null` → pas un dossier.
 * Reste = départ + frais ajoutés − somme des montants **FAIT** (centimes réels).
 */
object DossierRemaining {

    fun isDossier(openingRemainingCents: Long?): Boolean = openingRemainingCents != null

    /** Null si ce n’est pas un dossier. */
    fun remainingCents(
        openingRemainingCents: Long?,
        paidDoneCents: Long,
        addedFeesCents: Long = 0L
    ): Long? {
        val opening = openingRemainingCents ?: return null
        return opening + addedFeesCents.coerceAtLeast(0L) - paidDoneCents.coerceAtLeast(0L)
    }

    fun isSettled(
        openingRemainingCents: Long?,
        paidDoneCents: Long,
        addedFeesCents: Long = 0L
    ): Boolean {
        val rem = remainingCents(openingRemainingCents, paidDoneCents, addedFeesCents)
            ?: return false
        return rem <= 0L
    }

    /**
     * Versement du mois : forfait, ou reste dû s’il est positif et inférieur au forfait
     * (dernière ligne partielle). Les TODO ne comptent pas dans [paidDoneCents].
     */
    fun installmentCents(
        forfaitCents: Long,
        openingRemainingCents: Long?,
        paidDoneCents: Long,
        addedFeesCents: Long = 0L
    ): Long {
        val rem = remainingCents(openingRemainingCents, paidDoneCents, addedFeesCents)
            ?: return forfaitCents
        if (rem <= 0L) return forfaitCents
        return minOf(forfaitCents, rem)
    }
}
