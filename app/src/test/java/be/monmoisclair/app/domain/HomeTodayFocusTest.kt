package be.monmoisclair.app.domain

import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.After
import org.junit.Before
import org.junit.Test

class HomeTodayFocusTest {

    private val brussels: ZoneId = ZoneId.of("Europe/Brussels")

    @Before
    fun pinClock() {
        AppClock.clock = Clock.fixed(Instant.parse("2026-10-15T09:00:00Z"), brussels)
    }

    @After
    fun resetClock() {
        AppClock.clock = Clock.system(brussels)
    }

    @Test
    fun compute_overdue_and_dueToday_onCurrentMonth() {
        val ym = YearMonth.of(2026, 10)
        val expenses = listOf(
            HomeTodayFocus.ExpenseInput("a", "Loyer", 10, ExpenseStatus.TODO, 50_000L),
            HomeTodayFocus.ExpenseInput("b", "Élec", 15, ExpenseStatus.TODO, 8_000L),
            HomeTodayFocus.ExpenseInput("c", "Internet", 20, ExpenseStatus.TODO, 4_000L),
            HomeTodayFocus.ExpenseInput("d", "FAIT", 1, ExpenseStatus.DONE, 1_000L)
        )
        val result = HomeTodayFocus.compute(expenses, ym)
        assertThat(result.overdue.map { it.id }).containsExactly("a")
        assertThat(result.dueToday.map { it.id }).containsExactly("b")
        assertThat(result.firstExpenseId).isEqualTo("a")
        assertThat(result.headline()).isEqualTo("1 en retard · 1 aujourd’hui")
    }

    @Test
    fun compute_skipsSavings_and_futureMonth() {
        val ym = YearMonth.of(2026, 11)
        val expenses = listOf(
            HomeTodayFocus.ExpenseInput(
                "s",
                ExpenseOverdue.VOLUNTARY_SAVINGS_LABEL,
                1,
                ExpenseStatus.TODO,
                5_000L
            )
        )
        assertThat(HomeTodayFocus.compute(expenses, ym).hasAction).isFalse()
    }

    @Test
    fun isDueToday_falseWhenBeforeDue() {
        val ym = YearMonth.of(2026, 10)
        val today = LocalDate.of(2026, 10, 14)
        assertThat(HomeTodayFocus.isDueToday(15, ym, today)).isFalse()
    }

    @Test
    fun summarySectionLines_listsOverdueFirst() {
        val result = HomeTodayFocus.Result(
            overdue = listOf(HomeTodayFocus.Line("a", "Loyer", 50_000L)),
            dueToday = listOf(HomeTodayFocus.Line("b", "Gaz", 3_000L))
        )
        val lines = HomeTodayFocus.summarySectionLines(result)
        assertThat(lines[0]).contains("Aujourd’hui")
        assertThat(lines[1]).contains("En retard")
        assertThat(lines[2]).contains("À payer ce jour")
    }
}
