package be.monmoisclair.app.data.reminder

import be.monmoisclair.app.domain.model.ReminderKind

data class ReminderPayload(
    val expenseInstanceId: String,
    val kind: ReminderKind,
    val title: String,
    val body: String
)
