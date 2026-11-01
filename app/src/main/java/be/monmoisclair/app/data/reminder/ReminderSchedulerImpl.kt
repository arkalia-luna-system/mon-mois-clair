package be.monmoisclair.app.data.reminder

import android.util.Log
import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.domain.ExpenseOverdue
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.domain.model.ReminderKind
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderSchedulerImpl @Inject constructor(
    private val instanceDao: InstanceDao,
    private val alarmGateway: ExactAlarmGateway,
    private val appMetaDao: AppMetaDao
) : ReminderScheduler {

    override suspend fun reconcileAll() {
        val expenses = instanceDao.getAllExpenses()
        val hideAmounts = appMetaDao.get(AppMetaKeys.BIOMETRIC_ENABLED)?.value == "true"
        for (expense in expenses) {
            // P3-R01 : une facture mal datée ne doit pas couper toute la boucle.
            runCatching {
                when (expense.status) {
                    ExpenseStatus.DONE -> cancelFor(expense.id)
                    ExpenseStatus.TODO -> scheduleForTodo(expense, hideAmounts)
                }
            }.onFailure { Log.e(TAG, "rappel ignoré pour ${expense.id}", it) }
        }
    }

    override suspend fun cancelFor(expenseId: String) {
        alarmGateway.cancel(ReminderRequestCodes.of(expenseId, ReminderKind.J3))
        alarmGateway.cancel(ReminderRequestCodes.of(expenseId, ReminderKind.JD))
        alarmGateway.cancel(ReminderRequestCodes.of(expenseId, ReminderKind.OVERDUE))
    }

    private fun scheduleForTodo(expense: ExpenseInstanceEntity, hideAmounts: Boolean) {
        val yearMonth = runCatching {
            ReminderTiming.yearMonthFromLedgerId(expense.ledgerId)
        }.getOrNull() ?: return

        reconcileKind(
            expense = expense,
            kind = ReminderKind.J3,
            enabled = expense.remindJ3,
            triggerEpochMs = ReminderTiming.j3Instant(yearMonth, expense.dayOfMonth)?.toEpochMilli(),
            hideAmounts = hideAmounts
        )
        reconcileKind(
            expense = expense,
            kind = ReminderKind.JD,
            enabled = expense.remindJd,
            triggerEpochMs = ReminderTiming.jdInstant(yearMonth, expense.dayOfMonth)?.toEpochMilli(),
            hideAmounts = hideAmounts
        )

        val overdue = ExpenseOverdue.isOverdue(
            ledgerId = expense.ledgerId,
            dayOfMonth = expense.dayOfMonth,
            status = expense.status
        )
        reconcileKind(
            expense = expense,
            kind = ReminderKind.OVERDUE,
            enabled = overdue,
            triggerEpochMs = if (overdue) {
                ReminderTiming.nextOverdueInstant().toEpochMilli()
            } else {
                null
            },
            hideAmounts = hideAmounts
        )
    }

    private fun reconcileKind(
        expense: ExpenseInstanceEntity,
        kind: ReminderKind,
        enabled: Boolean,
        triggerEpochMs: Long?,
        hideAmounts: Boolean
    ) {
        val code = ReminderRequestCodes.of(expense.id, kind)
        if (!enabled || triggerEpochMs == null) {
            alarmGateway.cancel(code)
            return
        }
        val payload = ReminderPayload(
            expenseInstanceId = expense.id,
            kind = kind,
            title = ReminderNotifCopy.title(kind, expense.label, hideAmounts = hideAmounts),
            body = ReminderNotifCopy.body(
                kind = kind,
                amountCents = expense.amountCents,
                dayOfMonth = expense.dayOfMonth,
                isAutoDebit = expense.isAutoDebit,
                hideAmounts = hideAmounts
            )
        )
        alarmGateway.schedule(code, triggerEpochMs, payload)
    }

    companion object {
        private const val TAG = "ReminderScheduler"
    }
}
