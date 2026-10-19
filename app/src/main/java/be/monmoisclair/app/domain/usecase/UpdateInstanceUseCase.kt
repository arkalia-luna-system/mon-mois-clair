package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.MonthDayClamp
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

sealed class UpdateInstanceResult {
    data object Success : UpdateInstanceResult()
    data object NotFound : UpdateInstanceResult()
    data class InvalidInput(val message: String) : UpdateInstanceResult()
}

@Singleton
class UpdateInstanceUseCase @Inject constructor(
    private val instanceDao: InstanceDao,
    private val mutatingRepository: MutatingRepository
) {

    suspend operator fun invoke(
        id: String,
        isExpense: Boolean,
        label: String,
        amountCents: Long,
        dayOfMonth: Int,
        applyToFollowingMonths: Boolean
    ): UpdateInstanceResult {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) {
            return UpdateInstanceResult.InvalidInput("Le nom est obligatoire.")
        }
        if (amountCents <= 0L) {
            return UpdateInstanceResult.InvalidInput("Le montant doit être positif.")
        }
        if (dayOfMonth !in 1..31) {
            return UpdateInstanceResult.InvalidInput("Le jour doit être entre 1 et 31.")
        }

        val exists = if (isExpense) {
            instanceDao.getExpenseById(id) != null
        } else {
            instanceDao.getIncomeById(id) != null
        }
        if (!exists) return UpdateInstanceResult.NotFound

        return mutatingRepository.mutate(
            sideEffectsWhen = { it is UpdateInstanceResult.Success }
        ) {
            if (isExpense) {
                val current = instanceDao.getExpenseById(id)
                    ?: return@mutate UpdateInstanceResult.NotFound
                val ym = YearMonth.parse(current.ledgerId)
                val clamped = MonthDayClamp.clamp(dayOfMonth, ym)
                instanceDao.updateExpense(
                    current.copy(
                        label = trimmed,
                        amountCents = amountCents,
                        dayOfMonth = clamped
                    )
                )
                if (applyToFollowingMonths && current.templateId != null) {
                    val templateId = current.templateId
                    templateDao.getExpenseById(templateId)?.let { tpl ->
                        templateDao.updateExpense(
                            tpl.copy(
                                label = trimmed,
                                amountCents = amountCents,
                                dayOfMonth = dayOfMonth
                            )
                        )
                    }
                    val others = instanceDao.expensesFromTemplateFromLedger(
                        templateId,
                        current.ledgerId
                    ).filter { it.id != current.id && it.status == ExpenseStatus.TODO }
                    for (item in others) {
                        val itemYm = YearMonth.parse(item.ledgerId)
                        instanceDao.updateExpense(
                            item.copy(
                                label = trimmed,
                                amountCents = amountCents,
                                dayOfMonth = MonthDayClamp.clamp(dayOfMonth, itemYm)
                            )
                        )
                    }
                }
            } else {
                val current = instanceDao.getIncomeById(id)
                    ?: return@mutate UpdateInstanceResult.NotFound
                val ym = YearMonth.parse(current.ledgerId)
                val clamped = MonthDayClamp.clamp(dayOfMonth, ym)
                instanceDao.updateIncome(
                    current.copy(
                        label = trimmed,
                        amountCents = amountCents,
                        dayOfMonth = clamped
                    )
                )
                if (applyToFollowingMonths && current.templateId != null) {
                    val templateId = current.templateId
                    templateDao.getIncomeById(templateId)?.let { tpl ->
                        templateDao.updateIncome(
                            tpl.copy(
                                label = trimmed,
                                amountCents = amountCents,
                                dayOfMonth = dayOfMonth
                            )
                        )
                    }
                    val others = instanceDao.incomesFromTemplateFromLedger(
                        templateId,
                        current.ledgerId
                    ).filter { it.id != current.id }
                    for (item in others) {
                        val itemYm = YearMonth.parse(item.ledgerId)
                        instanceDao.updateIncome(
                            item.copy(
                                label = trimmed,
                                amountCents = amountCents,
                                dayOfMonth = MonthDayClamp.clamp(dayOfMonth, itemYm)
                            )
                        )
                    }
                }
            }
            UpdateInstanceResult.Success
        }
    }
}
