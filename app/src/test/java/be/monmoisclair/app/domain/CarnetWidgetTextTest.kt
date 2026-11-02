package be.monmoisclair.app.domain

import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class CarnetWidgetTextTest {

    private val today = LocalDate.of(2026, 10, 20)

    @Test
    fun picks_earliest_todo_and_skips_done() {
        val next = NextUnpaidLine.pick(
            expenses = listOf(
                expense("Loyer", day = 5, status = ExpenseStatus.DONE),
                expense("Énergie", day = 25, cents = 8_000),
                expense("Mutuelle", day = 12, cents = 4_500)
            ),
            ledgerId = "2026-10",
            today = today
        )
        assertThat(next!!.label).isEqualTo("Mutuelle")
        assertThat(next.amountCents).isEqualTo(4_500)
        assertThat(next.overdue).isTrue()
    }

    @Test
    fun same_day_uses_name_order() {
        val next = NextUnpaidLine.pick(
            expenses = listOf(
                expense("Zoo", day = 22),
                expense("Eau", day = 22)
            ),
            ledgerId = "2026-10",
            today = today
        )
        assertThat(next!!.label).isEqualTo("Eau")
        assertThat(next.overdue).isFalse()
    }

    @Test
    fun prefers_bill_over_voluntary_savings() {
        val next = NextUnpaidLine.pick(
            expenses = listOf(
                expense(ExpenseOverdue.VOLUNTARY_SAVINGS_LABEL, day = 1, cents = 2_000),
                expense("Gaz", day = 15, cents = 9_000)
            ),
            ledgerId = "2026-10",
            today = today
        )
        assertThat(next!!.label).isEqualTo("Gaz")
        // today = 20 oct. · Gaz jour 15 → en retard (facture réelle, pas l’épargne).
        assertThat(next.overdue).isTrue()
    }

    @Test
    fun savings_alone_is_never_marked_overdue() {
        val next = NextUnpaidLine.pick(
            expenses = listOf(
                expense(ExpenseOverdue.VOLUNTARY_SAVINGS_LABEL, day = 1, cents = 2_000)
            ),
            ledgerId = "2026-10",
            today = today
        )
        assertThat(next!!.label).isEqualTo(ExpenseOverdue.VOLUNTARY_SAVINGS_LABEL)
        assertThat(next.overdue).isFalse()
    }

    @Test
    fun open_month_shows_reste_and_next_line() {
        val text = CarnetWidgetText.compose(
            locked = false,
            summary = MonthSummary(200_000, 80_000, 120_000, 80_000),
            next = NextUnpaidLine.Line("Loyer", 80_000, overdue = false),
            expenseCount = 1
        )
        assertThat(text.title).isEqualTo("Reste")
        assertThat(text.amount).contains("1")
        assertThat(text.amount).contains("€")
        assertThat(text.amountNegative).isFalse()
        assertThat(text.line).startsWith("Loyer")
        assertThat(text.locked).isFalse()
    }

    @Test
    fun deficit_marks_amount_and_overdue_marks_line() {
        val text = CarnetWidgetText.compose(
            locked = false,
            summary = MonthSummary(10_000, 20_000, -10_000, 20_000),
            next = NextUnpaidLine.Line("Loyer", 20_000, overdue = true),
            expenseCount = 1
        )
        assertThat(text.amountNegative).isTrue()
        assertThat(text.line).startsWith("En retard · Loyer")
        assertThat(text.lineOverdue).isTrue()
    }

    @Test
    fun all_paid_and_empty_month() {
        val paid = CarnetWidgetText.compose(
            locked = false,
            summary = MonthSummary(0, 0, 0, 0),
            next = null,
            expenseCount = 2
        )
        assertThat(paid.line).isEqualTo("Tout est payé")
        val empty = CarnetWidgetText.compose(
            locked = false,
            summary = MonthSummary(0, 0, 0, 0),
            next = null,
            expenseCount = 0
        )
        assertThat(empty.line).isEqualTo("Rien à payer")
    }

    @Test
    fun lock_hides_amounts() {
        val text = CarnetWidgetText.compose(
            locked = true,
            summary = MonthSummary(200_000, 80_000, 120_000, 80_000),
            next = NextUnpaidLine.Line("Loyer", 80_000, overdue = true),
            expenseCount = 1
        )
        assertThat(text.locked).isTrue()
        assertThat(text.amount).isEmpty()
        assertThat(text.line).isEqualTo("Touchez pour ouvrir")
        assertThat(text.line).doesNotContain("Loyer")
        assertThat(text.amountNegative).isFalse()
    }

    private fun expense(
        label: String,
        day: Int,
        status: ExpenseStatus = ExpenseStatus.TODO,
        cents: Long = 1_000
    ) = ExpenseInstanceEntity(
        id = label,
        ledgerId = "2026-10",
        templateId = null,
        label = label,
        amountCents = cents,
        dayOfMonth = day,
        status = status,
        doneAtEpochMs = null,
        remindJ3 = true,
        remindJd = true,
        alarmJ3RequestCode = null,
        alarmJdRequestCode = null,
        createdAtEpochMs = 0L
    )
}
