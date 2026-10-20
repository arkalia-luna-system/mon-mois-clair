package be.monmoisclair.app.domain

import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MonthSummaryCalculatorTest {

    @Test
    fun compute_remaining_and_stillToPay() {
        val incomes = listOf(
            income("i1", 200_000L),
            income("i2", 50_000L)
        )
        val expenses = listOf(
            expense("e1", 47_000L, ExpenseStatus.DONE),
            expense("e2", 30_000L, ExpenseStatus.TODO),
            expense("e3", 20_000L, ExpenseStatus.TODO)
        )

        val summary = MonthSummaryCalculator.compute(incomes, expenses)
        assertThat(summary.totalIncomeCents).isEqualTo(250_000L)
        assertThat(summary.totalExpensesCents).isEqualTo(97_000L)
        assertThat(summary.remainingCents).isEqualTo(153_000L)
        assertThat(summary.stillToPayCents).isEqualTo(50_000L)
    }

    @Test
    fun compute_empty_isZero() {
        val summary = MonthSummaryCalculator.compute(emptyList(), emptyList())
        assertThat(summary.remainingCents).isEqualTo(0L)
        assertThat(summary.stillToPayCents).isEqualTo(0L)
    }

    @Test
    fun compute_negativeRemaining_whenExpensesExceedIncome() {
        val summary = MonthSummaryCalculator.compute(
            incomes = listOf(income("i1", 100_000L)),
            expenses = listOf(expense("e1", 150_000L, ExpenseStatus.DONE))
        )
        assertThat(summary.remainingCents).isEqualTo(-50_000L)
        // FAIT ne compte plus dans « à payer »
        assertThat(summary.stillToPayCents).isEqualTo(0L)
    }

    @Test
    fun afterDivers_isRemainingMinusDivers() {
        // Contrat Home : afterDiversCents = remainingCents - diversSpentCents
        val summary = MonthSummaryCalculator.compute(
            incomes = listOf(income("i1", 200_000L)),
            expenses = listOf(expense("e1", 50_000L, ExpenseStatus.TODO))
        )
        val divers = 12_500L
        assertThat(summary.remainingCents - divers).isEqualTo(137_500L)
    }

    private fun income(id: String, cents: Long) = IncomeInstanceEntity(
        id = id,
        ledgerId = "2026-10",
        templateId = null,
        label = id,
        amountCents = cents,
        dayOfMonth = 1,
        createdAtEpochMs = 1L
    )

    private fun expense(id: String, cents: Long, status: ExpenseStatus) = ExpenseInstanceEntity(
        id = id,
        ledgerId = "2026-10",
        templateId = null,
        label = id,
        amountCents = cents,
        dayOfMonth = 1,
        status = status,
        doneAtEpochMs = if (status == ExpenseStatus.DONE) 1L else null,
        remindJ3 = true,
        remindJd = true,
        alarmJ3RequestCode = null,
        alarmJdRequestCode = null,
        createdAtEpochMs = 1L
    )
}
