package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.repository.MutatingRepository
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

sealed class UpdateExpenseTemplateResult {
    data object Success : UpdateExpenseTemplateResult()
    data object NotFound : UpdateExpenseTemplateResult()
    data class InvalidInput(val message: String) : UpdateExpenseTemplateResult()
}

/**
 * Édition d’un modèle hors mois courant (montant, jour, ancre).
 * Les mois déjà matérialisés ne sont pas réécrits ici —
 * le prochain mois d’échéance utilisera le modèle mis à jour.
 */
@Singleton
class UpdateExpenseTemplateUseCase @Inject constructor(
    private val mutatingRepository: MutatingRepository
) {

    suspend operator fun invoke(
        templateId: String,
        label: String,
        amountCents: Long,
        dayOfMonth: Int,
        anchorYearMonth: YearMonth
    ): UpdateExpenseTemplateResult {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) {
            return UpdateExpenseTemplateResult.InvalidInput("Le nom est obligatoire.")
        }
        if (amountCents <= 0L) {
            return UpdateExpenseTemplateResult.InvalidInput("Le montant doit être positif.")
        }
        if (dayOfMonth !in 1..31) {
            return UpdateExpenseTemplateResult.InvalidInput("Le jour doit être entre 1 et 31.")
        }
        return mutatingRepository.mutate(
            sideEffectsWhen = { it is UpdateExpenseTemplateResult.Success }
        ) {
            val tpl = templateDao.getExpenseById(templateId)
                ?: return@mutate UpdateExpenseTemplateResult.NotFound
            templateDao.updateExpense(
                tpl.copy(
                    label = trimmed,
                    amountCents = amountCents,
                    dayOfMonth = dayOfMonth,
                    anchorYearMonth = anchorYearMonth.toString()
                )
            )
            UpdateExpenseTemplateResult.Success
        }
    }
}
