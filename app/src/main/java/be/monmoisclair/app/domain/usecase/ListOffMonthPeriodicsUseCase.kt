package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.RecurrenceAnchor
import be.monmoisclair.app.domain.RecurrenceRules
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

data class OffMonthPeriodic(
    val label: String,
    val amountCents: Long,
    val dayOfMonth: Int,
    val intervalMonths: Int,
    val nextDueYearMonth: String,
    val nextDueLabel: String
)

/**
 * T6 : templates actifs (intervalle > 1) qui ne tombent pas sur le mois courant.
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
                val anchor = RecurrenceRules.parseAnchorOrNull(t.anchorYearMonth) ?: return@mapNotNull null
                if (RecurrenceRules.shouldMaterialize(anchor, t.intervalMonths, current)) {
                    return@mapNotNull null
                }
                val next = nextOccurrence(anchor, t.intervalMonths, current)
                OffMonthPeriodic(
                    label = t.label,
                    amountCents = t.amountCents,
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
