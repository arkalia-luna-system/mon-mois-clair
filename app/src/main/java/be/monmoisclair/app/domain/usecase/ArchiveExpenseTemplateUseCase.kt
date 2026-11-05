package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.model.ExpenseStatus
import javax.inject.Inject
import javax.inject.Singleton

sealed class ArchiveExpenseTemplateResult {
    data object Success : ArchiveExpenseTemplateResult()
    data object NotFound : ArchiveExpenseTemplateResult()
}

/**
 * E02 : retirer une échéance hors mois (modèle sans ligne sur l’accueil).
 * Désactive le modèle ; les FAIT passés restent.
 * B4 : retire aussi les TODO des mois futurs déjà ouverts (aperçu).
 */
@Singleton
class ArchiveExpenseTemplateUseCase @Inject constructor(
    private val mutatingRepository: MutatingRepository,
    private val reminderScheduler: ReminderScheduler
) {

    suspend operator fun invoke(templateId: String): ArchiveExpenseTemplateResult {
        val removedIds = mutableListOf<String>()
        val result = mutatingRepository.mutate(
            sideEffectsWhen = { it is ArchiveExpenseTemplateResult.Success },
            rescheduleReminders = true
        ) {
            val tpl = templateDao.getExpenseById(templateId)
                ?: return@mutate ArchiveExpenseTemplateResult.NotFound
            if (!tpl.active) {
                return@mutate ArchiveExpenseTemplateResult.Success
            }
            templateDao.updateExpense(
                tpl.copy(
                    active = false,
                    archivedAtEpochMs = AppClock.nowEpochMs()
                )
            )
            val currentLedger = AppClock.ledgerId()
            val futures = instanceDao.expensesFromTemplateFromLedger(templateId, currentLedger)
                .filter {
                    it.ledgerId > currentLedger && it.status == ExpenseStatus.TODO
                }
            for (item in futures) {
                instanceDao.deleteExpense(item)
                removedIds += item.id
            }
            ArchiveExpenseTemplateResult.Success
        }
        if (result is ArchiveExpenseTemplateResult.Success) {
            removedIds.forEach { id ->
                runCatching { reminderScheduler.cancelFor(id) }
            }
        }
        return result
    }
}
