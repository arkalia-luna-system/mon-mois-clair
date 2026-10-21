package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
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
 *
 * Lot B — accords :
 * - FAIT qui solde → efface les TODO des mois suivants déjà ouverts ;
 * - Remettre → recrée les lignes manquantes sur les mois ouverts, en
 *   répartissant le reste chronologiquement (sans réécrire les montants existants).
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
            syncAgreementFutures(updated)

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

    /**
     * Après bascule :
     * - soldé (somme FAIT ≥ départ) → efface les TODO futures déjà créées ;
     * - Remettre (TODO) → recrée les absentes en répartissant le reste dû
     *   mois par mois (forfait ou reste partiel sur la première, rien après) ;
     * - FAIT intermédiaire (pas soldé) → ne touche pas aux futures.
     *
     * Les montants des lignes déjà présentes ne sont jamais réécrits.
     * Le mois courant TODO est déduit du reste avant répartition (un Remettre
     * du dernier paiement ne recrée donc rien si ce mois couvre déjà tout).
     */
    private suspend fun MutatingRepository.MutationContext.syncAgreementFutures(
        expense: ExpenseInstanceEntity
    ) {
        val templateId = expense.templateId ?: return
        val tpl = templateDao.getExpenseById(templateId) ?: return
        if (!DossierRemaining.isDossier(tpl.openingRemainingCents)) return

        val paid = instanceDao.sumAmountCentsByTemplateAndStatus(
            templateId,
            ExpenseStatus.DONE
        )
        val currentYm = YearMonth.parse(expense.ledgerId)

        if (DossierRemaining.isSettled(tpl.openingRemainingCents, paid, tpl.addedFeesCents)) {
            val futures = instanceDao.expensesFromTemplateFromLedger(
                templateId,
                expense.ledgerId
            ).filter {
                it.id != expense.id &&
                    it.status == ExpenseStatus.TODO &&
                    it.ledgerId > expense.ledgerId
            }
            for (item in futures) {
                instanceDao.deleteExpense(item)
            }
            return
        }

        // Recréation seulement après Remettre (ligne redevenue TODO).
        if (expense.status != ExpenseStatus.TODO) return

        val remaining = DossierRemaining.remainingCents(
            tpl.openingRemainingCents,
            paid,
            tpl.addedFeesCents
        ) ?: return

        // Ce mois redevient à payer : on le réserve avant les mois d’après.
        var pocket = remaining - expense.amountCents
        if (pocket < 0L) pocket = 0L
        if (pocket <= 0L) return

        val anchor = RecurrenceRules.parseAnchorOrNull(tpl.anchorYearMonth)
            ?: YearMonth.from(
                Instant.ofEpochMilli(tpl.createdAtEpochMs)
                    .atZone(AppClock.zone)
                    .toLocalDate()
            )
        val now = AppClock.nowEpochMs()
        for (ledger in monthLedgerDao.getAll().sortedBy { it.id }) {
            val ym = runCatching { YearMonth.parse(ledger.id) }.getOrNull() ?: continue
            if (!ym.isAfter(currentYm)) continue
            if (!RecurrenceRules.shouldMaterialize(anchor, tpl.intervalMonths, ym)) continue
            val id = InstanceIds.expenseFromTemplate(ledger.id, templateId)
            val existing = instanceDao.getExpenseById(id)
            if (existing != null) {
                // Ne jamais réécrire ; compter les TODO déjà là dans la répartition.
                if (existing.status == ExpenseStatus.TODO) {
                    pocket -= existing.amountCents
                    if (pocket < 0L) pocket = 0L
                }
                continue
            }
            if (pocket <= 0L) break
            val amount = minOf(tpl.amountCents, pocket)
            pocket -= amount
            instanceDao.insertExpense(
                ExpenseInstanceEntity(
                    id = id,
                    ledgerId = ledger.id,
                    templateId = templateId,
                    label = tpl.label,
                    amountCents = amount,
                    dayOfMonth = MonthDayClamp.clamp(tpl.dayOfMonth, ym),
                    status = ExpenseStatus.TODO,
                    doneAtEpochMs = null,
                    remindJ3 = tpl.remindJ3,
                    remindJd = tpl.remindJd,
                    isAutoDebit = tpl.isAutoDebit,
                    paymentNote = tpl.paymentNote,
                    receiptRelativePath = tpl.receiptRelativePath,
                    alarmJ3RequestCode = null,
                    alarmJdRequestCode = null,
                    createdAtEpochMs = now
                )
            )
        }
    }
}
