package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.MonthLedgerDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.InstanceIds
import be.monmoisclair.app.domain.MonthDayClamp
import be.monmoisclair.app.domain.PaymentNotePolicy
import be.monmoisclair.app.domain.RecurrenceRules
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.Instant
import java.time.YearMonth

/**
 * Lot B — synchro des lignes futures d’un accord (mois déjà ouverts).
 *
 * Deux modes :
 * - [afterToggle] : FAIT qui solde → efface ; Remettre (TODO) → recrée ;
 *   FAIT intermédiaire → ne touche pas (comportement historique).
 * - [afterAmountOrFeesChange] : après édition montant / frais / ouverture —
 *   efface si soldé ; sinon recrée les absentes **même si la ligne est DONE**
 *   (ex. baisse de montant ou frais ajoutés après un paiement qui avait
 *   déjà effacé le mois suivant).
 *
 * Les montants des lignes déjà présentes ne sont jamais réécrits.
 */
object AgreementFuturesSync {

    /**
     * Après bascule FAIT ↔ Remettre.
     * Recréation seulement si la ligne est TODO (Remettre).
     */
    suspend fun afterToggle(
        expense: ExpenseInstanceEntity,
        instanceDao: InstanceDao,
        templateDao: TemplateDao,
        monthLedgerDao: MonthLedgerDao
    ) {
        sync(
            expense = expense,
            instanceDao = instanceDao,
            templateDao = templateDao,
            monthLedgerDao = monthLedgerDao,
            recreateWhenCurrentIsDone = false
        )
    }

    /**
     * Après update d’une dépense liée à un dossier (montant, frais, reste…).
     * Recrée les futures manquantes dès que la dette n’est pas soldée,
     * y compris si le mois courant est déjà FAIT.
     */
    suspend fun afterAmountOrFeesChange(
        expense: ExpenseInstanceEntity,
        instanceDao: InstanceDao,
        templateDao: TemplateDao,
        monthLedgerDao: MonthLedgerDao
    ) {
        sync(
            expense = expense,
            instanceDao = instanceDao,
            templateDao = templateDao,
            monthLedgerDao = monthLedgerDao,
            recreateWhenCurrentIsDone = true
        )
    }

    /**
     * @param recreateWhenCurrentIsDone
     *   false = mode toggle (ignore FAIT non soldé) ;
     *   true = mode édition (recrée même en DONE si reste > 0).
     *
     * Le mois courant n’est réservé dans la répartition que s’il est TODO
     * (sinon son montant est déjà dans la somme FAIT / [paid]).
     */
    private suspend fun sync(
        expense: ExpenseInstanceEntity,
        instanceDao: InstanceDao,
        templateDao: TemplateDao,
        monthLedgerDao: MonthLedgerDao,
        recreateWhenCurrentIsDone: Boolean
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

        // Toggle : FAIT intermédiaire (dette pas finie) → ne pas toucher.
        if (!recreateWhenCurrentIsDone && expense.status != ExpenseStatus.TODO) return

        val remaining = DossierRemaining.remainingCents(
            tpl.openingRemainingCents,
            paid,
            tpl.addedFeesCents
        ) ?: return

        // Si ce mois est encore TODO, réserver son versement avant les suivants.
        // Les FAIT sont déjà dans [paid] — ne pas soustraire une seconde fois.
        val reserveCurrent = expense.status == ExpenseStatus.TODO
        var pocket = if (reserveCurrent) {
            (remaining - expense.amountCents).coerceAtLeast(0L)
        } else {
            remaining.coerceAtLeast(0L)
        }
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
                if (existing.status == ExpenseStatus.TODO) {
                    // B8 : plafonner une ligne déjà trop grosse (créée sans réserve).
                    if (pocket <= 0L) {
                        instanceDao.deleteExpense(existing)
                        continue
                    }
                    val capped = minOf(tpl.amountCents, pocket)
                    if (existing.amountCents != capped) {
                        instanceDao.updateExpense(existing.copy(amountCents = capped))
                    }
                    pocket -= capped
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
                    paymentNote = PaymentNotePolicy.noteForNewInstance(
                        templatePaymentNote = tpl.paymentNote,
                        openingRemainingCents = tpl.openingRemainingCents,
                        isAutoDebit = tpl.isAutoDebit
                    ),
                    receiptRelativePath = tpl.receiptRelativePath,
                    alarmJ3RequestCode = null,
                    alarmJdRequestCode = null,
                    createdAtEpochMs = now
                )
            )
        }
    }
}
