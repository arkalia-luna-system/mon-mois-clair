package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.PendingLetterDao
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.MonthCycleEngine
import be.monmoisclair.app.domain.MonthDayClamp
import be.monmoisclair.app.domain.InstanceIds
import be.monmoisclair.app.domain.RecurrenceRules
import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class ConvertPendingLetterResult {
    data class Success(val templateId: String, val instanceId: String?) : ConvertPendingLetterResult()
    data class InvalidInput(val message: String) : ConvertPendingLetterResult()
    data object NotFound : ConvertPendingLetterResult()
}

/**
 * Fixe un versement + reste dû → crée le dossier (template + ligne du mois) et retire le courrier.
 */
@Singleton
class ConvertPendingLetterToDossierUseCase @Inject constructor(
    private val pendingLetterDao: PendingLetterDao,
    private val monthCycleEngine: MonthCycleEngine,
    private val mutatingRepository: MutatingRepository
) {
    suspend operator fun invoke(
        letterId: String,
        monthlyAmountCents: Long,
        openingRemainingCents: Long,
        dayOfMonth: Int
    ): ConvertPendingLetterResult {
        if (monthlyAmountCents <= 0L) {
            return ConvertPendingLetterResult.InvalidInput("Le versement du mois doit être positif.")
        }
        if (openingRemainingCents <= 0L) {
            return ConvertPendingLetterResult.InvalidInput("Le reste dû doit être positif.")
        }
        if (dayOfMonth !in 1..31) {
            return ConvertPendingLetterResult.InvalidInput("Le jour doit être entre 1 et 31.")
        }
        val letter = pendingLetterDao.getById(letterId)
            ?: return ConvertPendingLetterResult.NotFound

        val ym = AppClock.currentYearMonth()
        monthCycleEngine.ensureMonthExists(ym)
        val ledgerId = AppClock.ledgerId(ym)
        val now = AppClock.nowEpochMs()
        val tid = "tpl-exp-${UUID.randomUUID()}"
        val materializeNow = RecurrenceRules.shouldMaterialize(ym, 1, ym)

        return mutatingRepository.mutate {
            templateDao.insertExpense(
                ExpenseTemplateEntity(
                    id = tid,
                    label = letter.who,
                    amountCents = monthlyAmountCents,
                    dayOfMonth = dayOfMonth,
                    active = true,
                    remindJ3 = true,
                    remindJd = true,
                    isAutoDebit = false,
                    intervalMonths = 1,
                    anchorYearMonth = ym.toString(),
                    paymentNote = letter.paymentNote,
                    receiptRelativePath = letter.receiptRelativePath,
                    causeLabel = letter.what,
                    openingRemainingCents = openingRemainingCents,
                    iban = letter.iban,
                    creditorEmail = letter.creditorEmail,
                    createdAtEpochMs = now,
                    archivedAtEpochMs = null
                )
            )
            val instanceId = if (materializeNow) {
                val clamped = MonthDayClamp.clamp(dayOfMonth, ym)
                val id = InstanceIds.expenseFromTemplate(ledgerId, tid)
                // B02 : 1ʳᵉ mensualité plafonnée au reste dû (comme MonthCycleEngine).
                val lineAmount = DossierRemaining.installmentCents(
                    forfaitCents = monthlyAmountCents,
                    openingRemainingCents = openingRemainingCents,
                    paidDoneCents = 0L,
                    addedFeesCents = 0L
                )
                instanceDao.insertExpense(
                    ExpenseInstanceEntity(
                        id = id,
                        ledgerId = ledgerId,
                        templateId = tid,
                        label = letter.who,
                        amountCents = lineAmount,
                        dayOfMonth = clamped,
                        status = ExpenseStatus.TODO,
                        doneAtEpochMs = null,
                        remindJ3 = true,
                        remindJd = true,
                        isAutoDebit = false,
                        paymentNote = letter.paymentNote,
                        receiptRelativePath = letter.receiptRelativePath,
                        alarmJ3RequestCode = null,
                        alarmJdRequestCode = null,
                        createdAtEpochMs = now
                    )
                )
                id
            } else {
                null
            }
            pendingLetterDao.deleteById(letterId)
            ConvertPendingLetterResult.Success(templateId = tid, instanceId = instanceId)
        }
    }
}
