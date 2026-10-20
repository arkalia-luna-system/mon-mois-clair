package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retire une échéance qui n’a pas encore de ligne sur l’accueil.
 * Les mois déjà notés ne sont pas effacés.
 */
@Singleton
class DeactivateExpenseTemplateUseCase @Inject constructor(
    private val mutatingRepository: MutatingRepository
) {
    suspend operator fun invoke(templateId: String): UpdateExpenseTemplateResult {
        return mutatingRepository.mutate(
            sideEffectsWhen = { it is UpdateExpenseTemplateResult.Success }
        ) {
            val tpl = templateDao.getExpenseById(templateId)
                ?: return@mutate UpdateExpenseTemplateResult.NotFound
            templateDao.updateExpense(
                tpl.copy(
                    active = false,
                    archivedAtEpochMs = AppClock.nowEpochMs()
                )
            )
            UpdateExpenseTemplateResult.Success
        }
    }
}
