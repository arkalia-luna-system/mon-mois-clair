package be.monmoisclair.app.ui.home

import be.monmoisclair.app.ui.format.MoneyFormat

/**
 * Libellé « À payer » / « Tout est payé » sous le reste-à-vivre.
 * Mois sans facture : rien (évite « À payer 0 € » qui rassure à tort).
 * Aperçu futur : « Prévu » (pas de FAIT sur ce mois).
 */
fun stickyStillToPayLabel(
    stillToPayCents: Long,
    hasExpenses: Boolean,
    futurePreview: Boolean = false,
    formatCents: (Long) -> String = MoneyFormat::formatCents
): String? {
    if (!hasExpenses) return null
    if (futurePreview) {
        return if (stillToPayCents == 0L) {
            null
        } else {
            "Prévu ${formatCents(stillToPayCents)}"
        }
    }
    return if (stillToPayCents == 0L) {
        "Tout est payé"
    } else {
        // 7.09 : libellé humain (plus de jargon « Factures ouvertes »).
        "Encore à payer ${formatCents(stillToPayCents)}"
    }
}

/**
 * Revenus − factures déjà FAIT (= Reste + À payer).
 * Seulement s’il reste des lignes à payer — pour voir ce qu’il resterait
 * si on ne comptait que ce qui est déjà réglé.
 * Masqué en aperçu futur (pas de FAIT possible).
 */
fun stickyWithoutUnpaidLabel(
    remainingCents: Long,
    stillToPayCents: Long,
    futurePreview: Boolean = false,
    formatCents: (Long) -> String = MoneyFormat::formatCents
): String? {
    if (futurePreview || stillToPayCents <= 0L) return null
    // Libellé familial : « Hors à payer » restait opaque (campagne Senior).
    return "Après les FAIT ${formatCents(remainingCents + stillToPayCents)}"
}
