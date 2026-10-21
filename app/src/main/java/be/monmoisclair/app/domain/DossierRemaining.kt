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

    /**
     * Estimation, jamais une date.
     * Mensuel : « Encore environ N mois si le versement reste de X ».
     * Autre rythme : « N versements », pour ne pas transformer un trimestre en mois calendaires.
     * Null si un **FAIT** n’est plus le forfait (le compte mentirait).
     * Les TODO plafonnées par [installmentCents] ne masquent pas ([todoAmounts] conservé pour l’appelant, ignoré ici).
     */
    fun pacePhrase(
        forfaitCents: Long,
        remainingCents: Long,
        intervalMonths: Int = 1,
        doneAmounts: List<Long> = emptyList(),
        @Suppress("UNUSED_PARAMETER") todoAmounts: List<Long> = emptyList()
    ): String? {
        if (forfaitCents <= 0L || remainingCents <= 0L) return null
        if (doneAmounts.any { it != forfaitCents }) return null
        val payments = (remainingCents + forfaitCents - 1) / forfaitCents
        if (payments <= 0L || payments > 1_200L) return null
        val n = payments.toInt()
        val monthly = intervalMonths.coerceAtLeast(1) == 1
        val unit = if (monthly) {
            if (n == 1) "1 mois" else "$n mois"
        } else {
            if (n == 1) "1 versement" else "$n versements"
        }
        val money = EuroFormat.formatCents(
            if (remainingCents < forfaitCents) remainingCents else forfaitCents
        )
        // Toujours « si » (principe 9) — jamais « fin prévue », jamais une date.
        return if (remainingCents < forfaitCents) {
            "Encore environ $unit si le prochain versement est le reste, $money."
        } else {
            "Encore environ $unit si le versement reste de $money."
        }
    }

    /** Rythme affiché à côté du forfait. Jamais un faux « / mois » pour 3 / 6 / 12 mois. */
    fun cadenceSuffix(intervalMonths: Int): String = when (val n = intervalMonths.coerceAtLeast(1)) {
        1 -> "/ mois"
        3 -> "/ trimestre"
        6 -> "/ semestre"
        12 -> "/ an"
        else -> "/ $n mois"
    }
}
