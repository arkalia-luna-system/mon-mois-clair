package be.monmoisclair.app.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import be.monmoisclair.app.domain.model.ReminderKind
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Déclenché par [ExactAlarmGatewayImpl].
 * Affiche la notification ; la replanification lourde reste dans [ReminderScheduler]
 * (reconcile au boot / après mutation) — étape 2.3.
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notifications: PaymentNotificationHelper

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_FIRE) return
        val pendingResult = goAsync()
        try {
            val expenseId = intent.getStringExtra(EXTRA_EXPENSE_ID) ?: return
            val kindName = intent.getStringExtra(EXTRA_KIND) ?: return
            val title = intent.getStringExtra(EXTRA_TITLE) ?: return
            val body = intent.getStringExtra(EXTRA_BODY) ?: return
            val kind = runCatching { ReminderKind.valueOf(kindName) }.getOrNull() ?: return
            val notificationId = ReminderRequestCodes.of(expenseId, kind)
            notifications.showPaymentReminder(notificationId, title, body)
        } finally {
            pendingResult.finish()
        }
    }

    companion object {
        const val ACTION_FIRE = "be.monmoisclair.app.action.REMINDER_FIRE"
        const val EXTRA_EXPENSE_ID = "expense_id"
        const val EXTRA_KIND = "kind"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
    }
}
