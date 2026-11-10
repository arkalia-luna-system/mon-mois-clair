package be.monmoisclair.app.domain.reminder

import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.domain.usecase.ToggleExpenseDoneResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReminderMarkUndoPolicyTest {

    @Test
    fun compensatesWhenUndoHitsTodoLine() {
        val toggled = ToggleExpenseDoneResult.Toggled(
            expenseId = "e",
            ledgerId = "2026-11",
            previousStatus = ExpenseStatus.TODO,
            newStatus = ExpenseStatus.DONE,
            doneAtEpochMs = 1L
        )
        assertThat(ReminderMarkUndoPolicy.needsCompensatingToggle(toggled)).isTrue()
    }

    @Test
    fun noCompensationWhenLegitimatelyUndoingDone() {
        val toggled = ToggleExpenseDoneResult.Toggled(
            expenseId = "e",
            ledgerId = "2026-11",
            previousStatus = ExpenseStatus.DONE,
            newStatus = ExpenseStatus.TODO,
            doneAtEpochMs = null
        )
        assertThat(ReminderMarkUndoPolicy.needsCompensatingToggle(toggled)).isFalse()
    }
}
