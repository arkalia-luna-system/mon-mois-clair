package be.monmoisclair.app.domain

import be.monmoisclair.app.data.db.dao.MonthLedgerDao
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.Instant
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton


sealed class EnsureMonthResult {
    data class Created(
        val ledgerId: String,
        val incomeCount: Int,
        val expenseCount: Int
    ) : EnsureMonthResult()

    data class AlreadyExists(val ledgerId: String) : EnsureMonthResult()
}

@Singleton
class MonthCycleEngine @Inject constructor(
    private val monthLedgerDao: MonthLedgerDao,
    private val mutatingRepository: MutatingRepository
) {

    suspend fun ensureCurrentMonthExists(): EnsureMonthResult =
        ensureMonthExists(AppClock.currentYearMonth())

    suspend fun ensureMonthExists(yearMonth: YearMonth): EnsureMonthResult {
        val ledgerId = AppClock.ledgerId(yearMonth)
        if (monthLedgerDao.getById(ledgerId) != null) {
            return EnsureMonthResult.AlreadyExists(ledgerId)
        }

        return mutatingRepository.mutate(
            sideEffectsWhen = { it is EnsureMonthResult.Created }
        ) {
            if (monthLedgerDao.getById(ledgerId) != null) {
                return@mutate EnsureMonthResult.AlreadyExists(ledgerId)
            }

            val now = AppClock.nowEpochMs()
            monthLedgerDao.insert(
                MonthLedgerEntity(
                    id = ledgerId,
                    yearMonth = yearMonth.toString(),
                    diversSpentCents = 0L,
                    createdAtEpochMs = now
                )
            )

            val incomeTemplates = templateDao.activeIncomeTemplates()
            val expenseTemplates = templateDao.activeExpenseTemplates()

            val incomes = incomeTemplates.map { template ->
                IncomeInstanceEntity(
                    id = InstanceIds.incomeFromTemplate(ledgerId, template.id),
                    ledgerId = ledgerId,
                    templateId = template.id,
                    label = template.label,
                    amountCents = template.amountCents,
                    dayOfMonth = MonthDayClamp.clamp(template.dayOfMonth, yearMonth),
                    createdAtEpochMs = now
                )
            }
            val expenses = expenseTemplates.mapNotNull { template ->
                val anchor = RecurrenceRules.parseAnchorOrNull(template.anchorYearMonth)
                    ?: YearMonth.from(
                        Instant.ofEpochMilli(template.createdAtEpochMs)
                            .atZone(AppClock.zone)
                            .toLocalDate()
                    )
                if (!RecurrenceRules.shouldMaterialize(anchor, template.intervalMonths, yearMonth)) {
                    return@mapNotNull null
                }
                // Lot B : accord soldé → plus de ligne. Dernier mois = le reste, pas la mensualité pleine.
                var amountCents = template.amountCents
                if (DossierRemaining.isDossier(template.openingRemainingCents)) {
                    val paid = instanceDao.sumAmountCentsByTemplateAndStatus(
                        template.id,
                        ExpenseStatus.DONE
                    )
                    val rem = DossierRemaining.remainingCents(template.openingRemainingCents, paid)
                    if (rem == null || rem <= 0L) return@mapNotNull null
                    if (rem < amountCents) amountCents = rem
                }
                ExpenseInstanceEntity(
                    id = InstanceIds.expenseFromTemplate(ledgerId, template.id),
                    ledgerId = ledgerId,
                    templateId = template.id,
                    label = template.label,
                    amountCents = amountCents,
                    dayOfMonth = MonthDayClamp.clamp(template.dayOfMonth, yearMonth),
                    status = ExpenseStatus.TODO,
                    doneAtEpochMs = null,
                    remindJ3 = template.remindJ3,
                    remindJd = template.remindJd,
                    isAutoDebit = template.isAutoDebit,
                    paymentNote = template.paymentNote,
                    receiptRelativePath = template.receiptRelativePath,
                    alarmJ3RequestCode = null,
                    alarmJdRequestCode = null,
                    createdAtEpochMs = now
                )
            }

            if (incomes.isNotEmpty()) {
                instanceDao.insertIncomes(incomes)
            }
            if (expenses.isNotEmpty()) {
                instanceDao.insertExpenses(expenses)
            }

            EnsureMonthResult.Created(
                ledgerId = ledgerId,
                incomeCount = incomes.size,
                expenseCount = expenses.size
            )
        }
    }
}
