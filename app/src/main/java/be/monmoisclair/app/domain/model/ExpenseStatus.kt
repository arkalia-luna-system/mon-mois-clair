package be.monmoisclair.app.domain.model

/**
 * Statut d'une dépense mensuelle. Stocké via TypeConverter Room — jamais de String libre.
 */
enum class ExpenseStatus {
    TODO,
    DONE
}
