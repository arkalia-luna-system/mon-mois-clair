package be.monmoisclair.app.domain

import java.time.YearMonth

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
     *
     * [reservedTodoCents] = versements TODO déjà posés sur des mois **antérieurs**
     * (ex. mois courant encore à payer) — à réserver avant de calculer le mois suivant.
     * Retourne 0 si plus rien à allouer (l’appelant doit omettre la ligne).
     */
    fun installmentCents(
        forfaitCents: Long,
        openingRemainingCents: Long?,
        paidDoneCents: Long,
        addedFeesCents: Long = 0L,
        reservedTodoCents: Long = 0L
    ): Long {
        val rem = remainingCents(openingRemainingCents, paidDoneCents, addedFeesCents)
            ?: return forfaitCents
        val available = rem - reservedTodoCents.coerceAtLeast(0L)
        if (available <= 0L) return 0L
        return minOf(forfaitCents, available)
    }

    /**
     * Nombre de versements restants (estimation, ceil). Null si non estimable.
     */
    fun remainingPaymentCount(
        forfaitCents: Long,
        remainingCents: Long
    ): Int? {
        if (forfaitCents <= 0L || remainingCents <= 0L) return null
        val payments = (remainingCents + forfaitCents - 1) / forfaitCents
        if (payments <= 0L || payments > 1_200L) return null
        return payments.toInt()
    }

    /** Chip courte Accueil (compact) : « encore ~N » — jamais les €. */
    fun shortPaceChip(remainingPayments: Int?): String? {
        val n = remainingPayments ?: return null
        if (n <= 0) return null
        return "encore ~$n"
    }

    /** Texte complet pour lecteur d’écran (pastille courte « encore ~N »). */
    fun shortPaceChipSpoken(remainingPayments: Int?): String? {
        val n = remainingPayments ?: return null
        if (n <= 0) return null
        return if (n == 1) {
            "environ une mensualité restante"
        } else {
            "environ $n mensualités restantes"
        }
    }

    /**
     * Estimation honnête : « Environ N mensualités » + « Dernière estimée : mois année ».
     * Jamais « fin prévue » certaine. Recalcule même si un FAIT ≠ forfait
     * (reste actuel ÷ forfait du modèle).
     */
    fun pacePhrase(
        forfaitCents: Long,
        remainingCents: Long,
        intervalMonths: Int = 1,
        doneAmounts: List<Long> = emptyList(),
        fromYearMonth: YearMonth = AppClock.currentYearMonth()
    ): String? {
        val n = remainingPaymentCount(forfaitCents, remainingCents) ?: return null
        val interval = intervalMonths.coerceAtLeast(1)
        val monthly = interval == 1
        val unit = if (monthly) {
            if (n == 1) "1 mensualité restante" else "$n mensualités restantes"
        } else {
            if (n == 1) "1 versement restant" else "$n versements restants"
        }
        val money = EuroFormat.formatCents(
            if (remainingCents < forfaitCents) remainingCents else forfaitCents
        )
        val head = if (remainingCents < forfaitCents) {
            "Environ $unit (reste $money)."
        } else {
            "Environ $unit."
        }
        val end = projectedEndYearMonth(remainingCents, forfaitCents, interval, fromYearMonth)
            ?: return head
        val whenLabel =
            "${RecurrenceAnchor.frenchMonthLabel(end.monthValue)} ${end.year}".lowercase()
        val diverged = doneAmounts.any { it != forfaitCents }
        val recalc = if (diverged) {
            " Un versement a changé : la date a été recalculée."
        } else {
            ""
        }
        return "$head Dernière estimée : $whenLabel.$recalc"
    }

    /**
     * Mois calendaire du dernier versement estimé (paiement de ce mois inclus).
     * Ex. 9 versements mensuels depuis oct. 2026 → juin 2027.
     */
    fun projectedEndYearMonth(
        remainingCents: Long,
        forfaitCents: Long,
        intervalMonths: Int = 1,
        from: YearMonth = AppClock.currentYearMonth()
    ): YearMonth? {
        if (forfaitCents <= 0L || remainingCents <= 0L) return null
        val payments = (remainingCents + forfaitCents - 1) / forfaitCents
        if (payments <= 0L || payments > 1_200L) return null
        val monthsAhead = (payments - 1L) * intervalMonths.coerceAtLeast(1).toLong()
        return from.plusMonths(monthsAhead)
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
