package be.monmoisclair.app.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.domain.usecase.ToggleExpenseDoneUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Action « FAIT » sur la notification (P3-Q01).
 * Ne bascule que TODO → DONE ; si déjà FAIT, ferme juste la notification.
 */
@AndroidEntryPoint
class MarkDoneReceiver : BroadcastReceiver() {

    @Inject
    lateinit var instanceDao: InstanceDao

    @Inject
    lateinit var toggleDone: ToggleExpenseDoneUseCase

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_MARK_DONE) return
        val expenseId = intent.getStringExtra(EXTRA_EXPENSE_ID) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val expense = instanceDao.getExpenseById(expenseId)
                if (expense != null && expense.status == ExpenseStatus.TODO) {
                    toggleDone(expenseId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "MarkDoneReceiver échec", e)
            } finally {
                if (notificationId >= 0) {
                    NotificationManagerCompat.from(context).cancel(notificationId)
                }
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "MarkDoneReceiver"
        const val ACTION_MARK_DONE = "be.monmoisclair.app.action.MARK_DONE"
        const val EXTRA_EXPENSE_ID = "expense_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
