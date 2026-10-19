package be.monmoisclair.app.data.reminder

/**
 * Replanifie / annule les alarmes exactes après mutation.
 * Implémentation réelle à l'étape 2.x.
 */
interface ReminderScheduler {
    suspend fun reconcileAll()
    suspend fun cancelFor(expenseId: String)
}
