package be.monmoisclair.app.domain

/**
 * Plafonds d’affichage pour listes potentiellement très longues (perf + mémoire Compose).
 * Accueil : appliquer **après** le filtre recherche (timeline complète dans le ViewModel).
 * Les totaux sticky / KPI restent calculés sur la liste complète en amont.
 */
object CarnetListLimits {

    const val MAX_HOME_TIMELINE_ITEMS = 400

    /** Dossiers soldés dépliés — le reste est indiqué en texte. */
    const val MAX_DOSSIER_SETTLED_VISIBLE = 80

    data class TruncateResult<T>(
        val visible: List<T>,
        val hiddenCount: Int
    )

    fun <T> truncate(
        items: List<T>,
        maxVisible: Int
    ): TruncateResult<T> {
        if (items.size <= maxVisible) {
            return TruncateResult(items, hiddenCount = 0)
        }
        return TruncateResult(
            visible = items.take(maxVisible),
            hiddenCount = items.size - maxVisible
        )
    }

    fun hiddenListHint(hiddenCount: Int): String? {
        if (hiddenCount <= 0) return null
        return if (hiddenCount == 1) {
            "1 ligne n’est pas affichée ici — affinez avec Chercher ou changez de mois."
        } else {
            "$hiddenCount lignes ne sont pas affichées ici — affinez avec Chercher ou changez de mois."
        }
    }

    fun hiddenDossiersHint(hiddenCount: Int): String? {
        if (hiddenCount <= 0) return null
        return if (hiddenCount == 1) {
            "1 dossier soldé de plus — filtrez ou ouvrez depuis la fiche."
        } else {
            "$hiddenCount dossiers soldés de plus — filtrez ou ouvrez depuis la fiche."
        }
    }
}
