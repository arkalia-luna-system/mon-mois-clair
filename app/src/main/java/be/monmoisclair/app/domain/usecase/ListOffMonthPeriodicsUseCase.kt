package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.RecurrenceAnchor
import be.monmoisclair.app.domain.RecurrenceRules
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

data class OffMonthPeriodic(
    val templateId: String,
    val label: String,
    val amountCents: Long,
    val dayOfMonth: Int,
    val intervalMonths: Int,
    val nextDueYearMonth: String,
    val nextDueLabel: String
)

/**
 * T6 : templates actifs (intervalle > 1) qui ne tombent pas sur le mois courant.
 * Les accords soldés n’apparaissent plus (même trimestriels / annuels).
 */
@Singleton
class ListOffMonthPeriodicsUseCase @Inject constructor(
    private val db: AppDatabase
) {

    suspend operator fun invoke(
        current: YearMonth = AppClock.currentYearMonth()
    ): List<OffMonthPeriodic> {
        return db.templateDao().activeExpenseTemplates()
            .filter { it.intervalMonths > 1 }
            .mapNotNull { t ->
                val paid = if (DossierRemaining.isDossier(t.openingRemainingCents)) {
                    db.instanceDao().sumAmountCentsByTemplateAndStatus(
                        t.id,
                        ExpenseStatus.DONE
                    )
                } else {
                    0L
                }
                if (DossierRemaining.isSettled(t.openingRemainingCents, paid, t.addedFeesCents)) {
                    return@mapNotNull null
                }
                val anchor = RecurrenceRules.parseAnchorOrNull(t.anchorYearMonth) ?: return@mapNotNull null
                if (RecurrenceRules.shouldMaterialize(anchor, t.intervalMonths, current)) {
                    return@mapNotNull null
                }
                val next = nextOccurrence(anchor, t.intervalMonths, current)
                val reservedTodo = if (DossierRemaining.isDossier(t.openingRemainingCents)) {
                    db.instanceDao().expensesByTemplate(t.id)
                        .asSequence()
                        .filter {
                            it.status == ExpenseStatus.TODO &&
                                it.ledgerId < next.toString()
                        }
                        .sumOf { it.amountCents }
                } else {
                    0L
                }
                val amount = DossierRemaining.installmentCents(
                    t.amountCents,
                    t.openingRemainingCents,
                    paid,
                    t.addedFeesCents,
                    reservedTodoCents = reservedTodo
                )
                if (amount <= 0L) return@mapNotNull null
                OffMonthPeriodic(
                    templateId = t.id,
                    label = t.label,
                    amountCents = amount,
                    dayOfMonth = t.dayOfMonth,
                    intervalMonths = t.intervalMonths,
                    nextDueYearMonth = next.toString(),
                    nextDueLabel = "${RecurrenceAnchor.frenchMonthLabel(next.monthValue)} ${next.year}"
                )
            }
            .sortedBy { it.nextDueYearMonth }
    }

    /** Prochaine occurrence ≥ [from] (ou > si [from] ne matérialise pas). */
    internal fun nextOccurrence(anchor: YearMonth, interval: Int, from: YearMonth): YearMonth {
        var m = if (from.isBefore(anchor)) anchor else from
        repeat(60) {
            if (RecurrenceRules.shouldMaterialize(anchor, interval, m)) return m
            m = m.plusMonths(1)
        }
        return anchor
    }
}
