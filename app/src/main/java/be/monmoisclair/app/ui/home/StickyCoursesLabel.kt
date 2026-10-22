package be.monmoisclair.app.ui.home

import be.monmoisclair.app.ui.format.MoneyFormat

/**
 * Ligne Courses du sticky Accueil (hors aperçu futur).
 * Montant divers visible ; « après » = Reste − courses (brique côté UI si négatif).
 * Toujours une entrée (même à 0) pour ouvrir l’édition depuis la bannière.
 */
fun stickyCoursesLabel(
    diversSpentCents: Long,
    afterDiversCents: Long,
    futurePreview: Boolean = false,
    formatCents: (Long) -> String = MoneyFormat::formatCents
): String? {
    if (futurePreview) return null
    return if (diversSpentCents > 0L) {
        "Courses ${formatCents(diversSpentCents)} · après ${formatCents(afterDiversCents)}"
    } else {
        "Courses"
    }
}
