package be.monmoisclair.app.domain

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.MonthLedgerDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.data.db.entity.IncomeTemplateEntity
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

    /**
     * Ledger déjà présent. [addedIncomeCount] / [addedExpenseCount] > 0 si des
     * templates manquants ont été ajoutés (ex. nouvelle facture après un aperçu futur).
     */
    data class AlreadyExists(
        val ledgerId: String,
        val addedIncomeCount: Int = 0,
        val addedExpenseCount: Int = 0
    ) : EnsureMonthResult()
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
        return mutatingRepository.mutate(
            sideEffectsWhen = { result ->
                when (result) {
                    is EnsureMonthResult.Created -> true
                    is EnsureMonthResult.AlreadyExists ->
                        result.addedIncomeCount > 0 || result.addedExpenseCount > 0
                }
            }
        ) {
            if (monthLedgerDao.getById(ledgerId) != null) {
                val added = syncMissingTemplates(
                    ledgerId = ledgerId,
                    yearMonth = yearMonth,
                    templateDao = templateDao,
                    instanceDao = instanceDao,
                    suppressedIds = SuppressedInstances.read(appMetaDao)
                )
                return@mutate EnsureMonthResult.AlreadyExists(
                    ledgerId = ledgerId,
                    addedIncomeCount = added.first,
                    addedExpenseCount = added.second
                )
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
            val incomes = buildIncomeInstances(ledgerId, yearMonth, now, incomeTemplates)
            val expenses = buildExpenseInstances(
                ledgerId,
                yearMonth,
                now,
                expenseTemplates,
                instanceDao
            )

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

    /**
     * Insère seulement les templates absents (ids stables). Ne touche pas aux lignes
     * déjà présentes (montants / FAIT figés).
     */
    private suspend fun syncMissingTemplates(
        ledgerId: String,
        yearMonth: YearMonth,
        templateDao: TemplateDao,
        instanceDao: InstanceDao,
        suppressedIds: Set<String>
    ): Pair<Int, Int> {
        val now = AppClock.nowEpochMs()
        val missingIncomes = buildIncomeInstances(
            ledgerId,
            yearMonth,
            now,
            templateDao.activeIncomeTemplates()
        ).filter {
            instanceDao.getIncomeById(it.id) == null && it.id !in suppressedIds
        }
        val missingExpenses = buildExpenseInstances(
            ledgerId,
            yearMonth,
            now,
            templateDao.activeExpenseTemplates(),
            instanceDao
        ).filter {
            instanceDao.getExpenseById(it.id) == null && it.id !in suppressedIds
        }
        if (missingIncomes.isNotEmpty()) {
            instanceDao.insertIncomes(missingIncomes)
        }
        if (missingExpenses.isNotEmpty()) {
            instanceDao.insertExpenses(missingExpenses)
        }
        return missingIncomes.size to missingExpenses.size
    }

    private fun buildIncomeInstances(
        ledgerId: String,
        yearMonth: YearMonth,
        now: Long,
        incomeTemplates: List<IncomeTemplateEntity>
    ): List<IncomeInstanceEntity> =
        incomeTemplates.map { template ->
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

    private suspend fun buildExpenseInstances(
        ledgerId: String,
        yearMonth: YearMonth,
        now: Long,
        expenseTemplates: List<ExpenseTemplateEntity>,
        instanceDao: InstanceDao
    ): List<ExpenseInstanceEntity> =
        expenseTemplates.mapNotNull { template ->
            val anchor = RecurrenceRules.parseAnchorOrNull(template.anchorYearMonth)
                ?: YearMonth.from(
                    Instant.ofEpochMilli(template.createdAtEpochMs)
                        .atZone(AppClock.zone)
                        .toLocalDate()
                )
            if (!RecurrenceRules.shouldMaterialize(anchor, template.intervalMonths, yearMonth)) {
                return@mapNotNull null
            }
            val amountCents = if (DossierRemaining.isDossier(template.openingRemainingCents)) {
                val paid = instanceDao.sumAmountCentsByTemplateAndStatus(
                    template.id,
                    ExpenseStatus.DONE
                )
                if (DossierRemaining.isSettled(
                        template.openingRemainingCents,
                        paid,
                        template.addedFeesCents
                    )
                ) {
                    return@mapNotNull null
                }
                DossierRemaining.installmentCents(
                    template.amountCents,
                    template.openingRemainingCents,
                    paid,
                    template.addedFeesCents
                )
            } else {
                template.amountCents
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
                paymentNote = PaymentNotePolicy.noteForNewInstance(
                    templatePaymentNote = template.paymentNote,
                    openingRemainingCents = template.openingRemainingCents,
                    isAutoDebit = template.isAutoDebit
                ),
                receiptRelativePath = template.receiptRelativePath,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = now
            )
        }
}
