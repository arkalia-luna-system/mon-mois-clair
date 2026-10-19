package be.monmoisclair.app.data.reminder

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
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
            when (expense.status) {
                ExpenseStatus.DONE -> cancelFor(expense.id)
                ExpenseStatus.TODO -> scheduleForTodo(expense)
            }
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
            title = titleFor(kind, expense.isAutoDebit),
            body = bodyFor(expense, kind)
        )
        alarmGateway.schedule(code, triggerEpochMs, payload)
    }

    private fun titleFor(kind: ReminderKind, autoDebit: Boolean): String = when (kind) {
        ReminderKind.J3 -> if (autoDebit) "Rappel prélèvement (J−3)" else "Rappel paiement (J−3)"
        ReminderKind.JD -> if (autoDebit) "Prélèvement aujourd'hui" else "Paiement aujourd'hui"
        ReminderKind.OVERDUE -> if (autoDebit) "Prélèvement en retard" else "Paiement en retard"
    }

    private fun bodyFor(expense: ExpenseInstanceEntity, kind: ReminderKind): String {
        val amount = formatEuro(expense.amountCents)
        val day = expense.dayOfMonth
        val auto = expense.isAutoDebit
        return when (kind) {
            ReminderKind.J3 ->
                if (auto) {
                    "${expense.label} — $amount — échéance le $day. Vérifiez le prélèvement."
                } else {
                    "${expense.label} — $amount — échéance le $day. Pensez à payer."
                }
            ReminderKind.JD ->
                if (auto) {
                    "${expense.label} — $amount — vérifiez le prélèvement aujourd'hui (le $day)."
                } else {
                    "${expense.label} — $amount — à payer aujourd'hui (le $day)."
                }
            ReminderKind.OVERDUE ->
                if (auto) {
                    "${expense.label} — $amount — prélèvement prévu le $day non confirmé. Vérifiez votre compte."
                } else {
                    "${expense.label} — $amount — échéance le $day dépassée. À régler."
                }
        }
    }

    private fun formatEuro(cents: Long): String {
        val sign = if (cents < 0) "-" else ""
        val abs = kotlin.math.abs(cents)
        val euros = abs / 100
        val rem = (abs % 100).toInt()
        return if (rem == 0) "$sign$euros €" else "%s%d,%02d €".format(sign, euros, rem)
    }
}
