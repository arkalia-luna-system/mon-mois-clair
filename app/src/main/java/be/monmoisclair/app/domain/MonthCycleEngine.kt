package be.monmoisclair.app.domain

import be.monmoisclair.app.data.db.dao.MonthLedgerDao
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.model.ExpenseStatus
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

/**
 * Cycle mensuel lazy : crée le ledger du mois demandé s'il n'existe pas,
 * en matérialisant une Instance (copie figée) pour chaque Template actif.
 *
 * Ne régénère JAMAIS un mois déjà créé (même si des templates ont changé depuis).
 */
@Singleton
class MonthCycleEngine @Inject constructor(
    private val monthLedgerDao: MonthLedgerDao,
    private val mutatingRepository: MutatingRepository
) {

    suspend fun ensureCurrentMonthExists(): EnsureMonthResult =
        ensureMonthExists(AppClock.currentYearMonth())

    /**
     * @param yearMonth mois cible (cold start = courant ; Home Préc./Suiv. = navigation).
     * Cold start : seul le mois courant. Navigation utilisateur : crée le mois tapé
     * (lazy) sans rattraper tout l’historique d’un coup.
     */
    suspend fun ensureMonthExists(yearMonth: YearMonth): EnsureMonthResult {
        val ledgerId = AppClock.ledgerId(yearMonth)
        if (monthLedgerDao.getById(ledgerId) != null) {
            return EnsureMonthResult.AlreadyExists(ledgerId)
        }

        return mutatingRepository.mutate(
            sideEffectsWhen = { it is EnsureMonthResult.Created }
        ) {
            // Double-check dans la transaction (course entre deux cold starts).
            // AlreadyExists ici ne doit PAS déclencher backup/reconcile.
            if (monthLedgerDao.getById(ledgerId) != null) {
                return@mutate EnsureMonthResult.AlreadyExists(ledgerId)
            }

            val now = AppClock.nowEpochMs()
            monthLedgerDao.insert(
                MonthLedgerEntity(
                    id = ledgerId,
                    yearMonth = yearMonth.toString(),
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
            val expenses = expenseTemplates.map { template ->
                ExpenseInstanceEntity(
                    id = InstanceIds.expenseFromTemplate(ledgerId, template.id),
                    ledgerId = ledgerId,
                    templateId = template.id,
                    label = template.label,
                    amountCents = template.amountCents,
                    dayOfMonth = MonthDayClamp.clamp(template.dayOfMonth, yearMonth),
                    status = ExpenseStatus.TODO,
                    doneAtEpochMs = null,
                    remindJ3 = template.remindJ3,
                    remindJd = template.remindJd,
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
