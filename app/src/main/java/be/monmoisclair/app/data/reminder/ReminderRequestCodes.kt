package be.monmoisclair.app.data.reminder

import be.monmoisclair.app.domain.model.ReminderKind
import java.util.Objects

object ReminderRequestCodes {

    /** RequestCode stable et positif pour AlarmManager / PendingIntent. */
    fun of(expenseId: String, kind: ReminderKind): Int =
        Objects.hash(expenseId, kind.name) and 0x7FFFFFFF
}
