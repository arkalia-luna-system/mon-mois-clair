package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import javax.inject.Inject
import javax.inject.Singleton

sealed class ArchiveExpenseTemplateResult {
    data object Success : ArchiveExpenseTemplateResult()
    data object NotFound : ArchiveExpenseTemplateResult()
}

/**
 * E02 : retirer une échéance hors mois (modèle sans ligne sur l’accueil).
 * Désactive le modèle ; les mois déjà notés ne changent pas.
 */
@Singleton
class ArchiveExpenseTemplateUseCase @Inject constructor(
    private val mutatingRepository: MutatingRepository
) {

    suspend operator fun invoke(templateId: String): ArchiveExpenseTemplateResult {
        return mutatingRepository.mutate(
            sideEffectsWhen = { it is ArchiveExpenseTemplateResult.Success },
            rescheduleReminders = false
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
            ArchiveExpenseTemplateResult.Success
        }
    }
}
