package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.ExpenseOverdue
import be.monmoisclair.app.domain.InstanceIds
import be.monmoisclair.app.domain.MonthDayClamp
import be.monmoisclair.app.domain.MonthSummaryCalculator
import be.monmoisclair.app.domain.SavingsProjection
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

data class SavingsAmountPreview(
    val remainingCents: Long,
    /** Ce mois est déjà FAIT : changer le montant ne le modifie pas. */
    val alreadyTransferredThisMonth: Boolean
)

data class SavingsAsideState(
    val active: Boolean,
    val amountCents: Long,
    val dayOfMonth: Int,
    val transferredCents: Long
)

sealed class ConfigureSavingsResult {
    data object Saved : ConfigureSavingsResult()
    data object Removed : ConfigureSavingsResult()
    data class InvalidInput(val message: String) : ConfigureSavingsResult()
}

/**
 * Lot E — une facture mensuelle « Mettre de côté », vers un autre compte.
 * Pas une cagnotte : elle entre dans le Reste comme les autres dépenses.
 * L’identifiant du modèle est dans `app_meta` (pas de colonne nouvelle).
 */
@Singleton
class ConfigureSavingsUseCase @Inject constructor(
    private val addEntry: AddEntryUseCase,
    private val mutatingRepository: MutatingRepository,
    private val instanceDao: InstanceDao,
    private val templateDao: TemplateDao,
    private val appMetaDao: AppMetaDao,
    private val reminderScheduler: ReminderScheduler
) {
    suspend fun read(): SavingsAsideState {
        val tpl = activeTemplate() ?: return SavingsAsideState(
            active = false,
            amountCents = 0L,
            dayOfMonth = 1,
            transferredCents = 0L
        )
        val transferred = instanceDao.sumAmountCentsByTemplateAndStatus(
            tpl.id,
            ExpenseStatus.DONE
        )
        return SavingsAsideState(
            active = true,
            amountCents = tpl.amountCents,
            dayOfMonth = tpl.dayOfMonth,
            transferredCents = transferred
        )
    }

    /** Reste du mois courant si on enregistrait [newCents] maintenant. */
    suspend fun previewAmount(newCents: Long): SavingsAmountPreview {
        val ledgerId = AppClock.ledgerId()
        val incomes = instanceDao.getAllIncomes().filter { it.ledgerId == ledgerId }
        val expenses = instanceDao.getAllExpenses().filter { it.ledgerId == ledgerId }
        val summary = MonthSummaryCalculator.compute(incomes, expenses)
        val line = activeTemplate()?.let { tpl ->
            expenses.firstOrNull { it.templateId == tpl.id }
        }
        val already = line?.status == ExpenseStatus.DONE
        val remaining = when {
            line == null ->
                SavingsProjection.remainingAfter(summary.remainingCents, 0L, newCents)
            already -> summary.remainingCents
            else -> SavingsProjection.remainingAfter(
                summary.remainingCents,
                line.amountCents,
                newCents
            )
        }
        return SavingsAmountPreview(
            remainingCents = remaining,
            alreadyTransferredThisMonth = already
        )
    }

    suspend fun save(amountCents: Long, dayOfMonth: Int): ConfigureSavingsResult {
        if (amountCents <= 0L) {
            return ConfigureSavingsResult.InvalidInput("Le montant doit être positif.")
        }
        if (dayOfMonth !in 1..31) {
            return ConfigureSavingsResult.InvalidInput("Le jour doit être entre 1 et 31.")
        }
        val existing = activeTemplate()
        if (existing == null) {
            when (
                val created = addEntry(
                    label = LABEL,
                    amountCents = amountCents,
                    dayOfMonth = dayOfMonth,
                    isExpense = true,
                    recurring = true,
                    intervalMonths = 1
                )
            ) {
                is AddEntryResult.InvalidInput ->
                    return ConfigureSavingsResult.InvalidInput(created.message)
                is AddEntryResult.Success -> {
                    val templateId = resolveTemplateId(created.instanceId)
                    if (templateId.isNullOrBlank()) {
                        return ConfigureSavingsResult.InvalidInput(
                            "La ligne n’a pas pu être créée. Réessayez."
                        )
                    }
                    mutatingRepository.mutate(rescheduleReminders = false) {
                        appMetaDao.upsert(
                            AppMetaEntity(AppMetaKeys.SAVINGS_TEMPLATE_ID, templateId)
                        )
                    }
                }
            }
            return ConfigureSavingsResult.Saved
        }
        mutatingRepository.mutate {
            val updated = existing.copy(amountCents = amountCents, dayOfMonth = dayOfMonth)
            templateDao.updateExpense(updated)
            val ledgerId = AppClock.ledgerId()
            val ymNow = AppClock.currentYearMonth()
            if (monthLedgerDao.getById(ledgerId) != null) {
                val currentId = InstanceIds.expenseFromTemplate(ledgerId, existing.id)
                if (instanceDao.getExpenseById(currentId) == null) {
                    instanceDao.insertExpense(
                        ExpenseInstanceEntity(
                            id = currentId,
                            ledgerId = ledgerId,
                            templateId = existing.id,
                            label = existing.label,
                            amountCents = amountCents,
                            dayOfMonth = MonthDayClamp.clamp(dayOfMonth, ymNow),
                            status = ExpenseStatus.TODO,
                            doneAtEpochMs = null,
                            remindJ3 = existing.remindJ3,
                            remindJd = existing.remindJd,
                            isAutoDebit = false,
                            paymentNote = existing.paymentNote,
                            receiptRelativePath = existing.receiptRelativePath,
                            alarmJ3RequestCode = null,
                            alarmJdRequestCode = null,
                            createdAtEpochMs = AppClock.nowEpochMs()
                        )
                    )
                }
            }
            val rows = instanceDao.expensesFromTemplateFromLedger(existing.id, ledgerId)
            for (item in rows) {
                if (item.status != ExpenseStatus.TODO) continue
                val ym = YearMonth.parse(item.ledgerId)
                instanceDao.updateExpense(
                    item.copy(
                        amountCents = amountCents,
                        dayOfMonth = MonthDayClamp.clamp(dayOfMonth, ym)
                    )
                )
            }
        }
        return ConfigureSavingsResult.Saved
    }

    suspend fun remove(): ConfigureSavingsResult {
        val existing = activeTemplate() ?: return ConfigureSavingsResult.Removed
        val removedIds = mutableListOf<String>()
        mutatingRepository.mutate {
            templateDao.updateExpense(
                existing.copy(active = false, archivedAtEpochMs = AppClock.nowEpochMs())
            )
            val from = AppClock.ledgerId()
            val rows = instanceDao.expensesFromTemplateFromLedger(existing.id, from)
            for (item in rows) {
                if (item.status == ExpenseStatus.TODO) {
                    instanceDao.deleteExpense(item)
                    removedIds += item.id
                }
            }
            appMetaDao.upsert(AppMetaEntity(AppMetaKeys.SAVINGS_TEMPLATE_ID, ""))
        }
        removedIds.forEach { id ->
            runCatching { reminderScheduler.cancelFor(id) }
        }
        return ConfigureSavingsResult.Removed
    }

    private suspend fun resolveTemplateId(createdId: String): String? {
        instanceDao.getExpenseById(createdId)?.templateId?.let { return it }
        return createdId.takeIf { templateDao.getExpenseById(it) != null }
    }

    private suspend fun activeTemplate(): ExpenseTemplateEntity? {
        val id = appMetaDao.get(AppMetaKeys.SAVINGS_TEMPLATE_ID)?.value?.trim().orEmpty()
        if (id.isEmpty()) return null
        val tpl = templateDao.getExpenseById(id) ?: return null
        if (!tpl.active) return null
        return tpl
    }

    companion object {
        const val LABEL = ExpenseOverdue.VOLUNTARY_SAVINGS_LABEL
    }
}
