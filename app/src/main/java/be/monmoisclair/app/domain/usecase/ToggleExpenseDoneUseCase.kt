package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.model.ExpenseStatus
import javax.inject.Inject
import javax.inject.Singleton

sealed class ToggleExpenseDoneResult {
    data class Toggled(
        val expenseId: String,
        val ledgerId: String,
        val previousStatus: ExpenseStatus,
        val newStatus: ExpenseStatus,
        val doneAtEpochMs: Long?
    ) : ToggleExpenseDoneResult()

    data object NotFound : ToggleExpenseDoneResult()
}

/**
 * Bascule FAIT : TODO ↔ DONE.
 * Autorisé sur **tout** mois déjà généré (courant ou précédent) —
 * pas de verrouillage lecture seule (oubli de cocher le 28).
 *
 * Passe par [MutatingRepository] → backup SAF + reconcile rappels
 * uniquement si une mutation réelle a lieu.
 */
@Singleton
class ToggleExpenseDoneUseCase @Inject constructor(
    private val instanceDao: InstanceDao,
    private val mutatingRepository: MutatingRepository
) {

    suspend operator fun invoke(expenseId: String): ToggleExpenseDoneResult {
        // Évite backup/reconcile inutiles si l'id n'existe pas.
        if (instanceDao.getExpenseById(expenseId) == null) {
            return ToggleExpenseDoneResult.NotFound
        }

        return mutatingRepository.mutate(
            sideEffectsWhen = { it is ToggleExpenseDoneResult.Toggled }
        ) {
            val existing = instanceDao.getExpenseById(expenseId)
                ?: return@mutate ToggleExpenseDoneResult.NotFound

            val updated = flip(existing)
            instanceDao.updateExpense(updated)

            ToggleExpenseDoneResult.Toggled(
                expenseId = updated.id,
                ledgerId = updated.ledgerId,
                previousStatus = existing.status,
                newStatus = updated.status,
                doneAtEpochMs = updated.doneAtEpochMs
            )
        }
    }

    /** Action notification FAIT : ne bascule pas Remettre si déjà DONE. */
    suspend fun markDoneIfTodo(expenseId: String): ToggleExpenseDoneResult {
        val existing = instanceDao.getExpenseById(expenseId)
            ?: return ToggleExpenseDoneResult.NotFound
        if (existing.status == ExpenseStatus.DONE) {
            return ToggleExpenseDoneResult.Toggled(
                expenseId = existing.id,
                ledgerId = existing.ledgerId,
                previousStatus = ExpenseStatus.DONE,
                newStatus = ExpenseStatus.DONE,
                doneAtEpochMs = existing.doneAtEpochMs
            )
        }
        return invoke(expenseId)
    }

    private fun flip(expense: ExpenseInstanceEntity): ExpenseInstanceEntity {
        return when (expense.status) {
            ExpenseStatus.TODO -> expense.copy(
                status = ExpenseStatus.DONE,
                doneAtEpochMs = AppClock.nowEpochMs()
            )
            ExpenseStatus.DONE -> expense.copy(
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null
            )
        }
    }
}
