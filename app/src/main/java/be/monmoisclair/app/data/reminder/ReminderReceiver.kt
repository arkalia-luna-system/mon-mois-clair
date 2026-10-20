package be.monmoisclair.app.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.domain.model.ReminderKind
import be.monmoisclair.app.domain.usecase.ToggleExpenseDoneResult
import be.monmoisclair.app.domain.usecase.ToggleExpenseDoneUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Déclenché par [ExactAlarmGatewayImpl] (FIRE) ou action notification FAIT (MARK_DONE).
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notifications: PaymentNotificationHelper

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    lateinit var toggleDone: ToggleExpenseDoneUseCase

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            ACTION_FIRE -> handleFire(intent)
            ACTION_MARK_DONE -> handleMarkDone(context, intent)
            ACTION_UNDO_DONE -> handleUndoDone(context, intent)
        }
    }

    private fun handleFire(intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val expenseId = intent.getStringExtra(EXTRA_EXPENSE_ID) ?: return@launch
                val kindName = intent.getStringExtra(EXTRA_KIND) ?: return@launch
                val title = intent.getStringExtra(EXTRA_TITLE) ?: return@launch
                val body = intent.getStringExtra(EXTRA_BODY) ?: return@launch
                val kind = runCatching { ReminderKind.valueOf(kindName) }.getOrNull()
                    ?: return@launch
                val notificationId = ReminderRequestCodes.of(expenseId, kind)
                notifications.showPaymentReminder(
                    notificationId = notificationId,
                    title = title,
                    body = body,
                    expenseInstanceId = expenseId
                )
                reminderScheduler.reconcileAll()
            } catch (e: Exception) {
                Log.e(TAG, "ReminderReceiver FIRE échec", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleMarkDone(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val expenseId = intent.getStringExtra(EXTRA_EXPENSE_ID) ?: return@launch
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
                val label = intent.getStringExtra(EXTRA_LABEL)?.ifBlank { null } ?: "Facture"
                val result = toggleDone.markDoneIfTodo(expenseId)
                val marked = result is ToggleExpenseDoneResult.Toggled &&
                    result.previousStatus == ExpenseStatus.TODO &&
                    result.newStatus == ExpenseStatus.DONE
                if (notificationId >= 0) {
                    if (marked) {
                        notifications.showMarkedDoneUndo(notificationId, label, expenseId)
                    } else {
                        NotificationManagerCompat.from(context).cancel(notificationId)
                    }
                }
                reminderScheduler.reconcileAll()
            } catch (e: Exception) {
                Log.e(TAG, "ReminderReceiver MARK_DONE échec", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleUndoDone(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val expenseId = intent.getStringExtra(EXTRA_EXPENSE_ID) ?: return@launch
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
                val result = toggleDone(expenseId)
                if (result is ToggleExpenseDoneResult.Toggled &&
                    result.previousStatus != ExpenseStatus.DONE
                ) {
                    // Déjà à payer : ne pas rebasculer vers FAIT.
                    if (result.newStatus == ExpenseStatus.DONE) {
                        toggleDone(expenseId)
                    }
                }
                if (notificationId >= 0) {
                    NotificationManagerCompat.from(context).cancel(notificationId)
                }
                reminderScheduler.reconcileAll()
            } catch (e: Exception) {
                Log.e(TAG, "ReminderReceiver UNDO échec", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "ReminderReceiver"
        const val ACTION_FIRE = "be.monmoisclair.app.action.REMINDER_FIRE"
        const val ACTION_MARK_DONE = "be.monmoisclair.app.action.REMINDER_MARK_DONE"
        const val ACTION_UNDO_DONE = "be.monmoisclair.app.action.REMINDER_UNDO_DONE"
        const val EXTRA_EXPENSE_ID = "expense_id"
        const val EXTRA_KIND = "kind"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_LABEL = "label"
    }
}
