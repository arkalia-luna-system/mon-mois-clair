package be.monmoisclair.app.ui.home

import be.monmoisclair.app.ui.format.MoneyFormat

/**
 * Libellé « À payer » / « Tout est payé » sous le reste-à-vivre.
 * Mois sans facture : rien (évite « À payer 0 € » qui rassure à tort).
 */
fun stickyStillToPayLabel(
    stillToPayCents: Long,
    hasExpenses: Boolean,
    formatCents: (Long) -> String = MoneyFormat::formatCents
): String? {
    if (!hasExpenses) return null
    return if (stillToPayCents == 0L) {
        "Tout est payé"
    } else {
        "À payer ${formatCents(stillToPayCents)}"
    }
}

/**
 * Revenus − factures déjà FAIT (= Reste + À payer).
 * Seulement s’il reste des lignes à payer — pour voir ce qu’il resterait
 * si on ne comptait que ce qui est déjà réglé.
 */
fun stickyWithoutUnpaidLabel(
    remainingCents: Long,
    stillToPayCents: Long,
    formatCents: (Long) -> String = MoneyFormat::formatCents
): String? {
    if (stillToPayCents <= 0L) return null
    return "Hors à payer ${formatCents(remainingCents + stillToPayCents)}"
}
