package be.monmoisclair.app.data.reminder

import javax.inject.Inject
import javax.inject.Singleton

/** Placeholder jusqu'à l'étape 2.x (AlarmManager). */
@Singleton
class NoOpReminderScheduler @Inject constructor() : ReminderScheduler {
    override suspend fun reconcileAll() = Unit
    override suspend fun cancelFor(expenseId: String) = Unit
}
