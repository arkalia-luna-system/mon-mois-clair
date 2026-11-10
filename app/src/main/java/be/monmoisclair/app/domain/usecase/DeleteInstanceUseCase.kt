package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.reminder.PaymentNotificationHelper
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.SuppressedInstances
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
    private val mutatingRepository: MutatingRepository,
    private val reminderScheduler: ReminderScheduler,
    private val paymentNotifications: PaymentNotificationHelper
) {

    /**
     * @param applyToFollowingMonths si true et template lié : désactive le modèle
     * et supprime les occurrences futures (ledgerId > courant) encore TODO (dépenses).
     */
    suspend operator fun invoke(
        id: String,
        isExpense: Boolean,
        applyToFollowingMonths: Boolean
    ): DeleteInstanceResult {
        val cancelledExpenseIds = mutableListOf<String>()
        val result = mutatingRepository.mutate(
            sideEffectsWhen = { it is DeleteInstanceResult.Success }
        ) {
            if (isExpense) {
                val current = instanceDao.getExpenseById(id)
                    ?: return@mutate DeleteInstanceResult.NotFound
                instanceDao.deleteExpense(current)
                cancelledExpenseIds += current.id
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
                    others.forEach { item ->
                        instanceDao.deleteExpense(item)
                        cancelledExpenseIds += item.id
                    }
                } else if (!applyToFollowingMonths && current.templateId != null) {
                    // Ce mois seulement : empêcher syncMissingTemplates de réinjecter.
                    SuppressedInstances.add(appMetaDao, current.id)
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
                } else if (!applyToFollowingMonths && current.templateId != null) {
                    SuppressedInstances.add(appMetaDao, current.id)
                }
            }
            DeleteInstanceResult.Success
        }
        // reconcileAll ne voit plus les lignes absentes : annuler chaque alarme orpheline.
        if (result is DeleteInstanceResult.Success) {
            cancelledExpenseIds.forEach { expenseId ->
                runCatching { paymentNotifications.cancelExpenseNotifications(expenseId) }
                runCatching { reminderScheduler.cancelFor(expenseId) }
            }
        }
        return result
    }
}
