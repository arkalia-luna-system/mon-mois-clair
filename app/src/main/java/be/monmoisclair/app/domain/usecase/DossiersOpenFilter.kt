package be.monmoisclair.app.domain.usecase

/**
 * Filtre rapide sur la liste « En cours » (seniors / multi-accords).
 */
enum class DossiersOpenFilter {
    ALL,
    /** En retard ce mois ou versement prévu ce mois (ouverts). */
    TO_HANDLE,
    /** Versement prévu ce mois seulement. */
    THIS_MONTH
}

object DossiersOpenFilterLogic {

    fun chipLabel(filter: DossiersOpenFilter): String = when (filter) {
        DossiersOpenFilter.ALL -> "Tous"
        DossiersOpenFilter.TO_HANDLE -> "À traiter"
        DossiersOpenFilter.THIS_MONTH -> "Ce mois"
    }

    fun matches(item: DossierListItem, filter: DossiersOpenFilter): Boolean {
        if (item.isSettled) return false
        return when (filter) {
            DossiersOpenFilter.ALL -> true
            DossiersOpenFilter.TO_HANDLE ->
                item.isOverdueThisMonth || item.dueThisMonth
            DossiersOpenFilter.THIS_MONTH -> item.dueThisMonth
        }
    }
}
