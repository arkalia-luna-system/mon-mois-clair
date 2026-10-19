package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.data.db.entity.IncomeTemplateEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.InstanceIds
import be.monmoisclair.app.domain.MonthCycleEngine
import be.monmoisclair.app.domain.MonthDayClamp
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class AddEntryResult {
    data class Success(val instanceId: String) : AddEntryResult()
    data class InvalidInput(val message: String) : AddEntryResult()
}

@Singleton
class AddEntryUseCase @Inject constructor(
    private val monthCycleEngine: MonthCycleEngine,
    private val mutatingRepository: MutatingRepository
) {

    suspend operator fun invoke(
        label: String,
        amountCents: Long,
        dayOfMonth: Int,
        isExpense: Boolean,
        recurring: Boolean
    ): AddEntryResult {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) {
            return AddEntryResult.InvalidInput("Le nom est obligatoire.")
        }
        if (amountCents <= 0L) {
            return AddEntryResult.InvalidInput("Le montant doit être positif.")
        }
        if (dayOfMonth !in 1..31) {
            return AddEntryResult.InvalidInput("Le jour doit être entre 1 et 31.")
        }

        monthCycleEngine.ensureCurrentMonthExists()
        val ym = AppClock.currentYearMonth()
        val ledgerId = AppClock.ledgerId(ym)
        val clamped = MonthDayClamp.clamp(dayOfMonth, ym)
        val now = AppClock.nowEpochMs()

        // InvalidInput est déjà filtré avant mutate → toujours Success ici.
        return mutatingRepository.mutate {
            if (isExpense) {
                val templateId = if (recurring) {
                    val tid = "tpl-exp-${UUID.randomUUID()}"
                    templateDao.insertExpense(
                        ExpenseTemplateEntity(
                            id = tid,
                            label = trimmed,
                            amountCents = amountCents,
                            dayOfMonth = dayOfMonth,
                            active = true,
                            remindJ3 = true,
                            remindJd = true,
                            createdAtEpochMs = now,
                            archivedAtEpochMs = null
                        )
                    )
                    tid
                } else {
                    null
                }
                val id = if (templateId != null) {
                    InstanceIds.expenseFromTemplate(ledgerId, templateId)
                } else {
                    InstanceIds.oneOffExpense()
                }
                instanceDao.insertExpense(
                    ExpenseInstanceEntity(
                        id = id,
                        ledgerId = ledgerId,
                        templateId = templateId,
                        label = trimmed,
                        amountCents = amountCents,
                        dayOfMonth = clamped,
                        status = ExpenseStatus.TODO,
                        doneAtEpochMs = null,
                        remindJ3 = true,
                        remindJd = true,
                        alarmJ3RequestCode = null,
                        alarmJdRequestCode = null,
                        createdAtEpochMs = now
                    )
                )
                AddEntryResult.Success(id)
            } else {
                val templateId = if (recurring) {
                    val tid = "tpl-inc-${UUID.randomUUID()}"
                    templateDao.insertIncome(
                        IncomeTemplateEntity(
                            id = tid,
                            label = trimmed,
                            amountCents = amountCents,
                            dayOfMonth = dayOfMonth,
                            active = true,
                            createdAtEpochMs = now,
                            archivedAtEpochMs = null
                        )
                    )
                    tid
                } else {
                    null
                }
                val id = if (templateId != null) {
                    InstanceIds.incomeFromTemplate(ledgerId, templateId)
                } else {
                    InstanceIds.oneOffIncome()
                }
                instanceDao.insertIncome(
                    IncomeInstanceEntity(
                        id = id,
                        ledgerId = ledgerId,
                        templateId = templateId,
                        label = trimmed,
                        amountCents = amountCents,
                        dayOfMonth = clamped,
                        createdAtEpochMs = now
                    )
                )
                AddEntryResult.Success(id)
            }
        }
    }
}
