package be.monmoisclair.app.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import be.monmoisclair.app.domain.model.ReminderKind
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Déclenché par [ExactAlarmGatewayImpl].
 * Affiche la notification puis **reprogramme** via [ReminderScheduler.reconcileAll]
 * (T7 : AlarmManager exact = one-shot ; sans ceci, OVERDUE ne sonne qu’une fois).
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notifications: PaymentNotificationHelper

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_FIRE) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val expenseId = intent.getStringExtra(EXTRA_EXPENSE_ID) ?: return@launch
                val kindName = intent.getStringExtra(EXTRA_KIND) ?: return@launch
                val title = intent.getStringExtra(EXTRA_TITLE) ?: return@launch
                val body = intent.getStringExtra(EXTRA_BODY) ?: return@launch
                val kind = runCatching { ReminderKind.valueOf(kindName) }.getOrNull() ?: return@launch
                val notificationId = ReminderRequestCodes.of(expenseId, kind)
                notifications.showPaymentReminder(
                    notificationId = notificationId,
                    title = title,
                    body = body,
                    expenseInstanceId = expenseId
                )
                // T7 : replanifie J+1 (OVERDUE) et toute alarme encore future.
                reminderScheduler.reconcileAll()
            } catch (e: Exception) {
                Log.e(TAG, "ReminderReceiver échec", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "ReminderReceiver"
        const val ACTION_FIRE = "be.monmoisclair.app.action.REMINDER_FIRE"
        const val EXTRA_EXPENSE_ID = "expense_id"
        const val EXTRA_KIND = "kind"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
    }
}
