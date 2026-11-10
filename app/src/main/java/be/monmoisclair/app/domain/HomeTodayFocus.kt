package be.monmoisclair.app.domain

import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Priorité du jour sur le mois en cours : factures en retard + échéance aujourd’hui.
 * Hors épargne volontaire ([ExpenseOverdue.VOLUNTARY_SAVINGS_LABEL]).
 */
object HomeTodayFocus {

    data class Line(
        val id: String,
        val label: String,
        val amountCents: Long = 0L
    )

    data class Result(
        val overdue: List<Line>,
        val dueToday: List<Line>
    ) {
        val hasAction: Boolean get() = overdue.isNotEmpty() || dueToday.isNotEmpty()

        val firstExpenseId: String?
            get() = overdue.firstOrNull()?.id ?: dueToday.firstOrNull()?.id

        fun headline(): String {
            val parts = mutableListOf<String>()
            val o = overdue.size
            val d = dueToday.size
            if (o > 0) {
                parts += if (o == 1) "1 en retard" else "$o en retard"
            }
            if (d > 0) {
                parts += if (d == 1) "1 aujourd’hui" else "$d aujourd’hui"
            }
            return parts.joinToString(" · ")
        }

        fun contentDescription(): String = when {
            !hasAction -> ""
            overdue.isNotEmpty() && dueToday.isEmpty() ->
                if (overdue.size == 1) {
                    "Une facture en retard aujourd’hui"
                } else {
                    "${overdue.size} factures en retard"
                }
            overdue.isEmpty() && dueToday.isNotEmpty() ->
                if (dueToday.size == 1) {
                    "Une facture à payer aujourd’hui"
                } else {
                    "${dueToday.size} factures à payer aujourd’hui"
                }
            else -> headline() + " à traiter aujourd’hui"
        }
    }

    data class ExpenseInput(
        val id: String,
        val label: String,
        val dayOfMonth: Int,
        val status: ExpenseStatus,
        val amountCents: Long = 0L
    )

    fun forCurrentMonth(
        yearMonth: YearMonth,
        today: LocalDate = AppClock.today()
    ): Boolean = yearMonth == YearMonth.from(today)

    fun compute(
        expenses: List<ExpenseInput>,
        yearMonth: YearMonth,
        today: LocalDate = AppClock.today(),
        ledgerId: String = AppClock.ledgerId(yearMonth)
    ): Result {
        if (!forCurrentMonth(yearMonth, today)) {
            return Result(emptyList(), emptyList())
        }
        val overdue = mutableListOf<Line>()
        val dueToday = mutableListOf<Line>()
        val sorted = expenses.sortedWith(
            compareBy<ExpenseInput> { it.dayOfMonth }.thenBy { it.label.lowercase() }
        )
        for (exp in sorted) {
            if (!ExpenseOverdue.appliesToLabel(exp.label)) continue
            if (exp.status != ExpenseStatus.TODO) continue
            val overdueFlag = ExpenseOverdue.isOverdue(
                ledgerId = ledgerId,
                dayOfMonth = exp.dayOfMonth,
                status = exp.status,
                today = today,
                label = exp.label
            )
            val line = Line(exp.id, exp.label.trim().ifBlank { "Facture" }, exp.amountCents)
            if (overdueFlag) {
                overdue += line
            } else if (isDueToday(exp.dayOfMonth, yearMonth, today)) {
                dueToday += line
            }
        }
        return Result(overdue, dueToday)
    }

    fun isDueToday(dayOfMonth: Int, yearMonth: YearMonth, today: LocalDate): Boolean {
        if (yearMonth != YearMonth.from(today)) return false
        val dueDay = MonthDayClamp.clamp(dayOfMonth, yearMonth)
        val due = yearMonth.atDay(dueDay)
        return !today.isBefore(due) && !today.isAfter(due)
    }

    fun formatDayHeader(today: LocalDate = AppClock.today()): String {
        val raw = today.format(DAY_HEADER)
        return raw.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString()
        }
    }

    fun summarySectionLines(result: Result, today: LocalDate = AppClock.today()): List<String> {
        if (!result.hasAction) return emptyList()
        val out = mutableListOf<String>()
        out += "Aujourd’hui (${formatDayHeader(today)})"
        for (line in result.overdue) {
            out += "• En retard : ${line.label} — ${EuroFormat.formatCents(line.amountCents)}"
        }
        for (line in result.dueToday) {
            out += "• À payer ce jour : ${line.label} — ${EuroFormat.formatCents(line.amountCents)}"
        }
        return out
    }

    private val DAY_HEADER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH)
}
