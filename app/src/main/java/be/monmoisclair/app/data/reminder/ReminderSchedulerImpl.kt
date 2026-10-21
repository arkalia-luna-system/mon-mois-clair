package be.monmoisclair.app.data.reminder

import android.util.Log
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.domain.EuroFormat
import be.monmoisclair.app.domain.ExpenseOverdue
import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.domain.model.ReminderKind
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderSchedulerImpl @Inject constructor(
    private val instanceDao: InstanceDao,
    private val alarmGateway: ExactAlarmGateway
) : ReminderScheduler {

    override suspend fun reconcileAll() {
        val expenses = instanceDao.getAllExpenses()
        for (expense in expenses) {
            // P3-R01 : une facture mal datée ne doit pas couper toute la boucle.
            runCatching {
                when (expense.status) {
                    ExpenseStatus.DONE -> cancelFor(expense.id)
                    ExpenseStatus.TODO -> scheduleForTodo(expense)
                }
            }.onFailure { Log.e(TAG, "rappel ignoré pour ${expense.id}", it) }
        }
    }

    override suspend fun cancelFor(expenseId: String) {
        alarmGateway.cancel(ReminderRequestCodes.of(expenseId, ReminderKind.J3))
        alarmGateway.cancel(ReminderRequestCodes.of(expenseId, ReminderKind.JD))
        alarmGateway.cancel(ReminderRequestCodes.of(expenseId, ReminderKind.OVERDUE))
    }

    private fun scheduleForTodo(expense: ExpenseInstanceEntity) {
        val yearMonth = runCatching {
            ReminderTiming.yearMonthFromLedgerId(expense.ledgerId)
        }.getOrNull() ?: return

        reconcileKind(
            expense = expense,
            kind = ReminderKind.J3,
            enabled = expense.remindJ3,
            triggerEpochMs = ReminderTiming.j3Instant(yearMonth, expense.dayOfMonth)?.toEpochMilli()
        )
        reconcileKind(
            expense = expense,
            kind = ReminderKind.JD,
            enabled = expense.remindJd,
            triggerEpochMs = ReminderTiming.jdInstant(yearMonth, expense.dayOfMonth)?.toEpochMilli()
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
            }
        )
    }

    private fun reconcileKind(
        expense: ExpenseInstanceEntity,
        kind: ReminderKind,
        enabled: Boolean,
        triggerEpochMs: Long?
    ) {
        val code = ReminderRequestCodes.of(expense.id, kind)
        if (!enabled || triggerEpochMs == null) {
            alarmGateway.cancel(code)
            return
        }
        val payload = ReminderPayload(
            expenseInstanceId = expense.id,
            kind = kind,
            title = titleFor(kind, expense.label),
            body = bodyFor(expense, kind)
        )
        alarmGateway.schedule(code, triggerEpochMs, payload)
    }

    private fun titleFor(kind: ReminderKind, label: String): String = when (kind) {
        ReminderKind.J3 -> "Dans 3 jours : $label"
        ReminderKind.JD -> "Aujourd’hui : $label"
        ReminderKind.OVERDUE -> "En retard : $label"
    }

    private fun bodyFor(expense: ExpenseInstanceEntity, kind: ReminderKind): String {
        val amount = EuroFormat.formatCents(expense.amountCents)
        val day = expense.dayOfMonth
        val auto = expense.isAutoDebit
        // Le titre porte déjà le libellé : le corps reste montant + action.
        return when (kind) {
            ReminderKind.J3 ->
                if (auto) {
                    "$amount — échéance le $day. Vérifiez le prélèvement."
                } else {
                    "$amount — échéance le $day. Pensez à payer."
                }
            ReminderKind.JD ->
                if (auto) {
                    "$amount — vérifiez le prélèvement aujourd'hui (le $day)."
                } else {
                    "$amount — à payer aujourd'hui (le $day)."
                }
            ReminderKind.OVERDUE ->
                if (auto) {
                    "$amount — prélèvement prévu le $day non confirmé. Vérifiez votre compte."
                } else {
                    "$amount — échéance le $day dépassée. À régler."
                }
        }
    }

    companion object {
        private const val TAG = "ReminderScheduler"
    }
}
