package be.monmoisclair.app.domain

import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class ExpenseOverdueTest {

    @Test
    fun not_overdue_before_due_day() {
        assertThat(
            ExpenseOverdue.isOverdue(
                ledgerId = "2026-10",
                dayOfMonth = 15,
                status = ExpenseStatus.TODO,
                today = LocalDate.of(2026, 10, 15)
            )
        ).isFalse()
    }

    @Test
    fun overdue_after_due_day_if_todo() {
        assertThat(
            ExpenseOverdue.isOverdue(
                ledgerId = "2026-10",
                dayOfMonth = 15,
                status = ExpenseStatus.TODO,
                today = LocalDate.of(2026, 10, 16)
            )
        ).isTrue()
    }

    @Test
    fun done_never_overdue() {
        assertThat(
            ExpenseOverdue.isOverdue(
                ledgerId = "2026-10",
                dayOfMonth = 1,
                status = ExpenseStatus.DONE,
                today = LocalDate.of(2026, 10, 31)
            )
        ).isFalse()
    }

    @Test
    fun future_month_never_overdue() {
        assertThat(
            ExpenseOverdue.isOverdue(
                ledgerId = "2026-11",
                dayOfMonth = 1,
                status = ExpenseStatus.TODO,
                today = LocalDate.of(2026, 10, 22)
            )
        ).isFalse()
    }
}
