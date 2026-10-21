package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.InstanceIds
import be.monmoisclair.app.domain.MonthDayClamp
import be.monmoisclair.app.domain.RecurrenceRules
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.Instant
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

sealed class ToggleExpenseDoneResult {
    data class Toggled(
        val expenseId: String,
        val ledgerId: String,
        val previousStatus: ExpenseStatus,
        val newStatus: ExpenseStatus,
        val doneAtEpochMs: Long?
    ) : ToggleExpenseDoneResult()

    data object NotFound : ToggleExpenseDoneResult()
}

/**
 * Bascule FAIT : TODO ↔ DONE.
 * Autorisé sur **tout** mois déjà généré (courant ou précédent) —
 * pas de verrouillage lecture seule (oubli de cocher le 28).
 *
 * Passe par [MutatingRepository] → backup SAF + reconcile rappels
 * uniquement si une mutation réelle a lieu.
 */
@Singleton
class ToggleExpenseDoneUseCase @Inject constructor(
    private val instanceDao: InstanceDao,
    private val mutatingRepository: MutatingRepository
) {

    suspend operator fun invoke(expenseId: String): ToggleExpenseDoneResult {
        // Évite backup/reconcile inutiles si l'id n'existe pas.
        if (instanceDao.getExpenseById(expenseId) == null) {
            return ToggleExpenseDoneResult.NotFound
        }

        return mutatingRepository.mutate(
            sideEffectsWhen = { it is ToggleExpenseDoneResult.Toggled }
        ) {
            val existing = instanceDao.getExpenseById(expenseId)
                ?: return@mutate ToggleExpenseDoneResult.NotFound

            val updated = flip(existing)
            instanceDao.updateExpense(updated)
            syncAgreementAfterToggle(updated)

            ToggleExpenseDoneResult.Toggled(
                expenseId = updated.id,
                ledgerId = updated.ledgerId,
                previousStatus = existing.status,
                newStatus = updated.status,
                doneAtEpochMs = updated.doneAtEpochMs
            )
        }
    }

    /** Action notification FAIT : ne bascule pas Remettre si déjà DONE. */
    suspend fun markDoneIfTodo(expenseId: String): ToggleExpenseDoneResult {
        val existing = instanceDao.getExpenseById(expenseId)
            ?: return ToggleExpenseDoneResult.NotFound
        if (existing.status == ExpenseStatus.DONE) {
            return ToggleExpenseDoneResult.Toggled(
                expenseId = existing.id,
                ledgerId = existing.ledgerId,
                previousStatus = ExpenseStatus.DONE,
                newStatus = ExpenseStatus.DONE,
                doneAtEpochMs = existing.doneAtEpochMs
            )
        }
        return invoke(expenseId)
    }

    /**
     * Accord soldé : retire les lignes encore « à payer » des mois d’après
     * (même si le mois a déjà été ouvert).
     * « Remettre » : ces lignes reviennent, sans dépasser le reste.
     */
    private suspend fun MutatingRepository.MutationContext.syncAgreementAfterToggle(
        updated: ExpenseInstanceEntity
    ) {
        val templateId = updated.templateId ?: return
        val template = templateDao.getExpenseById(templateId) ?: return
        val opening = template.openingRemainingCents ?: return
        val paid = instanceDao.sumAmountCentsByTemplateAndStatus(templateId, ExpenseStatus.DONE)
        if (DossierRemaining.isSettled(opening, paid)) {
            val futures = instanceDao.expensesFromTemplateFromLedger(templateId, updated.ledgerId)
                .filter { it.ledgerId > updated.ledgerId && it.status == ExpenseStatus.TODO }
            for (item in futures) {
                instanceDao.deleteExpense(item)
            }
            return
        }
        if (updated.status != ExpenseStatus.TODO) return
        val anchor = RecurrenceRules.parseAnchorOrNull(template.anchorYearMonth)
            ?: YearMonth.from(
                Instant.ofEpochMilli(template.createdAtEpochMs)
                    .atZone(AppClock.zone)
                    .toLocalDate()
            )
        var pocket = (DossierRemaining.remainingCents(opening, paid) ?: return) - updated.amountCents
        if (pocket < 0L) pocket = 0L
        val futureLedgers = monthLedgerDao.getAll()
            .map { it.id }
            .filter { it > updated.ledgerId }
            .sorted()
        for (ledgerId in futureLedgers) {
            val ym = runCatching { YearMonth.parse(ledgerId) }.getOrNull() ?: continue
            if (!RecurrenceRules.shouldMaterialize(anchor, template.intervalMonths, ym)) continue
            val id = InstanceIds.expenseFromTemplate(ledgerId, templateId)
            val existing = instanceDao.getExpenseById(id)
            if (existing != null) {
                if (existing.status == ExpenseStatus.TODO) pocket -= existing.amountCents
                if (pocket < 0L) pocket = 0L
                continue
            }
            if (pocket <= 0L) continue
            val amount = minOf(template.amountCents, pocket)
            insertAgreementInstance(template, ledgerId, ym, id, amount)
            pocket -= amount
        }
    }

    private suspend fun MutatingRepository.MutationContext.insertAgreementInstance(
        template: ExpenseTemplateEntity,
        ledgerId: String,
        yearMonth: YearMonth,
        id: String,
        amountCents: Long
    ) {
        instanceDao.insertExpense(
            ExpenseInstanceEntity(
                id = id,
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
                createdAtEpochMs = AppClock.nowEpochMs()
            )
        )
    }

    private fun flip(expense: ExpenseInstanceEntity): ExpenseInstanceEntity {
        return when (expense.status) {
            ExpenseStatus.TODO -> expense.copy(
                status = ExpenseStatus.DONE,
                doneAtEpochMs = AppClock.nowEpochMs()
            )
            ExpenseStatus.DONE -> expense.copy(
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null
            )
        }
    }
}
