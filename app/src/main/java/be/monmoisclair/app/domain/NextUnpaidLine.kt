package be.monmoisclair.app.domain

import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.LocalDate
import java.time.YearMonth

/** Prochaine facture encore TODO : la plus proche, puis le nom. */
object NextUnpaidLine {

    data class Line(
        val label: String,
        val amountCents: Long,
        val overdue: Boolean
    )

    fun pick(
        expenses: List<ExpenseInstanceEntity>,
        ledgerId: String,
        today: LocalDate = AppClock.today()
    ): Line? {
        val todo = expenses.filter { it.status == ExpenseStatus.TODO }
        if (todo.isEmpty()) return null
        // Préférer une vraie facture : l’épargne volontaire ne vole pas la ligne widget.
        val bills = todo.filter { ExpenseOverdue.appliesToLabel(it.label) }
        val pool = bills.ifEmpty { todo }
        val ym = runCatching { YearMonth.parse(ledgerId) }.getOrNull()
        val chosen = pool.minWith(
            compareBy<ExpenseInstanceEntity>(
                { dueEpochDay(it, ym) },
                { it.label }
            )
        )
        return Line(
            label = chosen.label,
            amountCents = chosen.amountCents,
            overdue = ExpenseOverdue.isOverdue(
                ledgerId = ledgerId,
                dayOfMonth = chosen.dayOfMonth,
                status = chosen.status,
                today = today,
                label = chosen.label
            )
        )
    }

    private fun dueEpochDay(expense: ExpenseInstanceEntity, ym: YearMonth?): Long {
        if (ym == null) return expense.dayOfMonth.toLong()
        val day = MonthDayClamp.clamp(expense.dayOfMonth, ym)
        return ym.atDay(day).toEpochDay()
    }
}
