package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.model.ExpenseStatus
import javax.inject.Inject
import javax.inject.Singleton

sealed class DeleteInstanceResult {
    data object Success : DeleteInstanceResult()
    data object NotFound : DeleteInstanceResult()
}

@Singleton
class DeleteInstanceUseCase @Inject constructor(
    private val instanceDao: InstanceDao,
    private val mutatingRepository: MutatingRepository
) {

    /**
     * @param applyToFollowingMonths si true et template lié : désactive le template
     * et supprime les occurrences futures (ledgerId > courant) encore TODO (dépenses).
     */
    suspend operator fun invoke(
        id: String,
        isExpense: Boolean,
        applyToFollowingMonths: Boolean
    ): DeleteInstanceResult {
        return mutatingRepository.mutate(
            sideEffectsWhen = { it is DeleteInstanceResult.Success }
        ) {
            if (isExpense) {
                val current = instanceDao.getExpenseById(id)
                    ?: return@mutate DeleteInstanceResult.NotFound
                instanceDao.deleteExpense(current)
                if (applyToFollowingMonths && current.templateId != null) {
                    val templateId = current.templateId
                    templateDao.getExpenseById(templateId)?.let { tpl ->
                        templateDao.updateExpense(
                            tpl.copy(
                                active = false,
                                archivedAtEpochMs = AppClock.nowEpochMs()
                            )
                        )
                    }
                    val others = instanceDao.expensesFromTemplateFromLedger(
                        templateId,
                        current.ledgerId
                    ).filter {
                        it.id != current.id &&
                            it.ledgerId > current.ledgerId &&
                            it.status == ExpenseStatus.TODO
                    }
                    others.forEach { instanceDao.deleteExpense(it) }
                }
            } else {
                val current = instanceDao.getIncomeById(id)
                    ?: return@mutate DeleteInstanceResult.NotFound
                instanceDao.deleteIncome(current)
                if (applyToFollowingMonths && current.templateId != null) {
                    val templateId = current.templateId
                    templateDao.getIncomeById(templateId)?.let { tpl ->
                        templateDao.updateIncome(
                            tpl.copy(
                                active = false,
                                archivedAtEpochMs = AppClock.nowEpochMs()
                            )
                        )
                    }
                    val others = instanceDao.incomesFromTemplateFromLedger(
                        templateId,
                        current.ledgerId
                    ).filter { it.id != current.id && it.ledgerId > current.ledgerId }
                    others.forEach { instanceDao.deleteIncome(it) }
                }
            }
            DeleteInstanceResult.Success
        }
    }
}
