package be.monmoisclair.app.ui.home

import be.monmoisclair.app.domain.DossierRemaining

/**
 * Pastilles statut facture Accueil — max 2, hors Compose pour tests.
 *
 * Priorité (compact = critique seulement) :
 * 1. En retard / FAIT
 * 2. encore ~N (rythme accord)
 * 3. Accord
 * 4. Prélèvement (jamais avant le rythme)
 */
object ExpenseStatusChips {

    fun labels(
        isOverdue: Boolean,
        isDone: Boolean,
        doneLabel: String?,
        isAutoDebit: Boolean,
        isAgreement: Boolean,
        hideAgreementChip: Boolean,
        remainingPayments: Int?,
        compact: Boolean
    ): List<String> {
        val out = mutableListOf<String>()
        when {
            isOverdue && !isDone -> out += "En retard"
            isDone -> out += (doneLabel ?: "FAIT ✓")
        }
        if (compact) return out.take(2)
        if (isAgreement && out.size < 2) {
            DossierRemaining.shortPaceChip(remainingPayments)?.let { out += it }
        }
        if (isAgreement && !hideAgreementChip && out.size < 2) {
            out += "Accord"
        }
        if (isAutoDebit && out.size < 2) {
            out += "Prélèvement"
        }
        return out.take(2)
    }
}
