package be.monmoisclair.app.domain.reminder

import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.domain.usecase.ToggleExpenseDoneResult

/**
 * Action « Annuler » sur la notif après FAIT :
 * si la ligne était déjà À payer, le 1ᵉr toggle la mettrait FAIT → on rebascule.
 */
object ReminderMarkUndoPolicy {

    fun needsCompensatingToggle(result: ToggleExpenseDoneResult.Toggled): Boolean =
        result.previousStatus != ExpenseStatus.DONE && result.newStatus == ExpenseStatus.DONE
}
