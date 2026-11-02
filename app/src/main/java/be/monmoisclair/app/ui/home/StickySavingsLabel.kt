package be.monmoisclair.app.ui.home

import be.monmoisclair.app.ui.format.MoneyFormat

/**
 * Ligne « Mettre de côté » du sticky Accueil (hors aperçu futur).
 * Comme Courses : toujours une entrée pour ouvrir l’édition — pas un 2ᵉ hero.
 */
fun stickySavingsLabel(
    savingsMonthlyCents: Long,
    futurePreview: Boolean = false,
    formatCents: (Long) -> String = MoneyFormat::formatCents
): String? {
    if (futurePreview) return null
    return if (savingsMonthlyCents > 0L) {
        "Mettre de côté ${formatCents(savingsMonthlyCents)}"
    } else {
        "Mettre de côté"
    }
}

/**
 * Chip 7.09 (icône tirelire) : montant si saisi, sinon « Côté ».
 * TalkBack = [stickySavingsLabel] (« Mettre de côté »).
 */
fun stickySavingsChipLabel(
    savingsMonthlyCents: Long,
    futurePreview: Boolean = false,
    formatCents: (Long) -> String = MoneyFormat::formatCents
): String? {
    if (futurePreview) return null
    return if (savingsMonthlyCents > 0L) formatCents(savingsMonthlyCents) else "Côté"
}
