package be.monmoisclair.app.data.reminder

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
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
    }

    private fun scheduleForTodo(expense: ExpenseInstanceEntity) {
        val yearMonth = runCatching {
            ReminderTiming.yearMonthFromLedgerId(expense.ledgerId)
        }.getOrNull() ?: return

        reconcileKind(
            expense = expense,
            kind = ReminderKind.J3,
            enabled = expense.remindJ3,
            trigger = ReminderTiming.j3Instant(yearMonth, expense.dayOfMonth)
        )
        reconcileKind(
            expense = expense,
            kind = ReminderKind.JD,
            enabled = expense.remindJd,
            trigger = ReminderTiming.jdInstant(yearMonth, expense.dayOfMonth)
        )
    }

    private fun reconcileKind(
        expense: ExpenseInstanceEntity,
        kind: ReminderKind,
        enabled: Boolean,
        trigger: java.time.Instant?
    ) {
        val code = ReminderRequestCodes.of(expense.id, kind)
        if (!enabled || trigger == null) {
            alarmGateway.cancel(code)
            return
        }
        val payload = ReminderPayload(
            expenseInstanceId = expense.id,
            kind = kind,
            title = titleFor(kind),
            body = bodyFor(expense, kind)
        )
        alarmGateway.schedule(code, trigger.toEpochMilli(), payload)
    }

    private fun titleFor(kind: ReminderKind): String = when (kind) {
        ReminderKind.J3 -> "Rappel paiement (J−3)"
        ReminderKind.JD -> "Paiement aujourd'hui"
    }

    private fun bodyFor(expense: ExpenseInstanceEntity, kind: ReminderKind): String {
        val amount = formatEuro(expense.amountCents)
        val day = expense.dayOfMonth
        return when (kind) {
            ReminderKind.J3 ->
                "${expense.label} — $amount — échéance le $day. Pensez à payer."
            ReminderKind.JD ->
                "${expense.label} — $amount — à payer aujourd'hui (le $day)."
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
