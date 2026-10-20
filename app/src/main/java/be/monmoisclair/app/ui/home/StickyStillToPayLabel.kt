package be.monmoisclair.app.ui.home

import be.monmoisclair.app.ui.format.MoneyFormat

/**
 * Libellé sous le reste-à-vivre.
 * Mois sans facture : rien (évite « À payer 0 € » qui rassure à tort).
 * Factures toutes payées : « Tout est payé ».
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
