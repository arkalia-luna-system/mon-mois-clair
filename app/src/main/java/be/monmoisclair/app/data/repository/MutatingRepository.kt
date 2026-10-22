package be.monmoisclair.app.data.repository

import androidx.room.withTransaction
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.dao.ExpenseFeeEventDao
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.MonthLedgerDao
import be.monmoisclair.app.data.db.dao.PendingLetterDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import android.util.Log
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.widget.CarnetWidgetSink
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Point d'entrée UNIQUE de toute mutation.
 *
 * Ordre strict :
 * 1. Transaction Room
 * 2. Enqueue backup SAF (si succès **et** side-effect autorisé)
 * 3. Reconcile rappels (si demandé et side-effect autorisé)
 *
 * En cas d'échec de transaction : aucun backup, aucune alarme.
 * [sideEffectsWhen] évite backup/reconcile pour un no-op (ex. AlreadyExists / NotFound
 * découvert dans la transaction après une course).
 */
@Singleton
class MutatingRepository @Inject constructor(
    private val db: AppDatabase,
    private val backupScheduler: BackupScheduler,
    private val reminderScheduler: ReminderScheduler,
    private val widgetSink: CarnetWidgetSink = CarnetWidgetSink.Noop
) {

    suspend fun <T> mutate(
        rescheduleReminders: Boolean = true,
        sideEffectsWhen: (T) -> Boolean = { true },
        block: suspend MutationContext.() -> T
    ): T {
        val result = db.withTransaction {
            val context = MutationContext(
                monthLedgerDao = db.monthLedgerDao(),
                templateDao = db.templateDao(),
                instanceDao = db.instanceDao(),
                appMetaDao = db.appMetaDao(),
                pendingLetterDao = db.pendingLetterDao(),
                feeEventDao = db.expenseFeeEventDao()
            )
            context.block()
        }
        if (sideEffectsWhen(result)) {
            backupScheduler.enqueueImmediate()
            if (rescheduleReminders) {
                reminderScheduler.reconcileAll()
            }
        }
        try {
            widgetSink.refresh()
        } catch (e: Exception) {
            Log.e("MutatingRepository", "widget non rafraîchi", e)
        }
        return result
    }

    class MutationContext(
        val monthLedgerDao: MonthLedgerDao,
        val templateDao: TemplateDao,
        val instanceDao: InstanceDao,
        val appMetaDao: AppMetaDao,
        val pendingLetterDao: PendingLetterDao,
        val feeEventDao: ExpenseFeeEventDao
    )
}
