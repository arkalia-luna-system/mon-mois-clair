package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.RecurrenceRules
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

data class NextMonthPeriodic(val label: String, val amountCents: Long, val dayOfMonth: Int)

@Singleton
class ListNextMonthPeriodicsUseCase @Inject constructor(private val db: AppDatabase) {
    suspend operator fun invoke(current: YearMonth = AppClock.currentYearMonth()): List<NextMonthPeriodic> {
        val next = current.plusMonths(1)
        return db.templateDao().activeExpenseTemplates()
            .filter { it.intervalMonths > 1 }
            .mapNotNull { t ->
                val anchor = RecurrenceRules.parseAnchorOrNull(t.anchorYearMonth) ?: return@mapNotNull null
                if (!RecurrenceRules.shouldMaterialize(anchor, t.intervalMonths, next)) {
                    return@mapNotNull null
                }
                if (DossierRemaining.isDossier(t.openingRemainingCents)) {
                    val paid = db.instanceDao().sumAmountCentsByTemplateAndStatus(
                        t.id,
                        ExpenseStatus.DONE
                    )
                    if (DossierRemaining.isSettled(
                            t.openingRemainingCents,
                            paid,
                            t.addedFeesCents
                        )
                    ) {
                        return@mapNotNull null
                    }
                }
                NextMonthPeriodic(t.label, t.amountCents, t.dayOfMonth)
            }
            .sortedBy { it.dayOfMonth }
    }
}
