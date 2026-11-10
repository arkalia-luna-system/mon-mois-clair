package be.monmoisclair.app.data.reminder

import be.monmoisclair.app.domain.model.ReminderKind

/** IDs SystemUI pour une facture (une notif par [ReminderKind]). */
object PaymentNotificationCancel {
    fun notificationIdsForExpense(expenseInstanceId: String): List<Int> =
        ReminderKind.entries.map { ReminderRequestCodes.of(expenseInstanceId, it) }
}
