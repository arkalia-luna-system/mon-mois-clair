package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.MonthDayClamp
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.YearMonth
import java.util.UUID
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

    /**
     * @param intervalMonths
     *   null = ne pas changer la récurrence ;
     *   -1 = passer en « une seule fois » (détache + archive le modèle) ;
     *   1..24 = intervalle (crée le modèle si la ligne était ponctuelle).
     */
    suspend operator fun invoke(
        id: String,
        isExpense: Boolean,
        label: String,
        amountCents: Long,
        dayOfMonth: Int,
        applyToFollowingMonths: Boolean,
        isAutoDebit: Boolean = false,
        paymentNote: String = "",
        receiptRelativePath: String = "",
        intervalMonths: Int? = null
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
        if (intervalMonths != null && intervalMonths != -1 && intervalMonths !in 1..24) {
            return UpdateInstanceResult.InvalidInput("Fréquence invalide.")
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
                var current = instanceDao.getExpenseById(id)
                    ?: return@mutate UpdateInstanceResult.NotFound
                val ym = YearMonth.parse(current.ledgerId)
                val clamped = MonthDayClamp.clamp(dayOfMonth, ym)
                val auto = isAutoDebit
                val receipt = receiptRelativePath.trim()
                    .ifEmpty { current.receiptRelativePath }

                when {
                    intervalMonths == -1 && current.templateId != null -> {
                        val tid = current.templateId!!
                        templateDao.getExpenseById(tid)?.let { tpl ->
                            if (tpl.active) {
                                templateDao.updateExpense(
                                    tpl.copy(
                                        active = false,
                                        archivedAtEpochMs = AppClock.nowEpochMs()
                                    )
                                )
                            }
                        }
                        val futureTodos = instanceDao.expensesFromTemplateFromLedger(
                            tid,
                            current.ledgerId
                        ).filter { it.id != current.id && it.status == ExpenseStatus.TODO }
                        for (item in futureTodos) {
                            instanceDao.deleteExpense(item)
                        }
                        current = current.copy(templateId = null)
                    }
                    intervalMonths != null && intervalMonths > 0 -> {
                        val interval = intervalMonths.coerceIn(1, 24)
                        if (current.templateId == null) {
                            val tid = "tpl-exp-${UUID.randomUUID()}"
                            val now = AppClock.nowEpochMs()
                            templateDao.insertExpense(
                                ExpenseTemplateEntity(
                                    id = tid,
                                    label = trimmed,
                                    amountCents = amountCents,
                                    dayOfMonth = dayOfMonth,
                                    active = true,
                                    remindJ3 = true,
                                    remindJd = true,
                                    isAutoDebit = auto,
                                    intervalMonths = interval,
                                    anchorYearMonth = current.ledgerId,
                                    paymentNote = paymentNote.trim(),
                                    receiptRelativePath = if (applyToFollowingMonths) {
                                        receipt
                                    } else {
                                        ""
                                    },
                                    createdAtEpochMs = now,
                                    archivedAtEpochMs = null
                                )
                            )
                            current = current.copy(templateId = tid)
                        } else {
                            templateDao.getExpenseById(current.templateId)?.let { tpl ->
                                templateDao.updateExpense(
                                    tpl.copy(intervalMonths = interval)
                                )
                            }
                        }
                    }
                }

                val note = paymentNote.trim()
                instanceDao.updateExpense(
                    current.copy(
                        label = trimmed,
                        amountCents = amountCents,
                        dayOfMonth = clamped,
                        isAutoDebit = auto,
                        paymentNote = note,
                        receiptRelativePath = receipt
                    )
                )

                // Lot A : la communication suit toujours le modèle (même sans « mois suivants »).
                // La photo modèle seulement si « Aussi les mois suivants ».
                if (current.templateId != null) {
                    val templateId = current.templateId
                    templateDao.getExpenseById(templateId)?.let { tpl ->
                        templateDao.updateExpense(
                            if (applyToFollowingMonths) {
                                tpl.copy(
                                    label = trimmed,
                                    amountCents = amountCents,
                                    dayOfMonth = dayOfMonth,
                                    isAutoDebit = auto,
                                    intervalMonths = when {
                                        intervalMonths != null && intervalMonths > 0 ->
                                            intervalMonths.coerceIn(1, 24)
                                        else -> tpl.intervalMonths
                                    },
                                    paymentNote = note,
                                    receiptRelativePath = receipt
                                )
                            } else {
                                tpl.copy(paymentNote = note)
                            }
                        )
                    }
                    if (applyToFollowingMonths) {
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
                                    dayOfMonth = MonthDayClamp.clamp(dayOfMonth, itemYm),
                                    isAutoDebit = auto,
                                    paymentNote = note,
                                    receiptRelativePath = receipt
                                )
                            )
                        }
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
