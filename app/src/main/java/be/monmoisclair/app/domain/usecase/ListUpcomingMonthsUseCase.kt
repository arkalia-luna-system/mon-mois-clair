package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.MonthNavigation
import be.monmoisclair.app.domain.RecurrenceAnchor
import be.monmoisclair.app.domain.RecurrenceRules
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

data class UpcomingLine(
    val label: String,
    val amountCents: Long,
    val dayOfMonth: Int,
    val isIncome: Boolean,
    val cadenceHint: String,
    val isAgreement: Boolean
)

data class UpcomingMonth(
    val yearMonth: YearMonth,
    val monthLabel: String,
    val lines: List<UpcomingLine>
)

/**
 * Aperçu lecture seule des M+1 … M+[horizon] (revenus + dépenses qui tomberaient).
 * N’écrit rien en base — le mois courant reste la seule source de vérité pour FAIT.
 */
@Singleton
class ListUpcomingMonthsUseCase @Inject constructor(
    private val db: AppDatabase
) {
    suspend operator fun invoke(
        from: YearMonth = AppClock.currentYearMonth(),
        horizonMonths: Int = MonthNavigation.UPCOMING_PREVIEW_MONTHS
    ): List<UpcomingMonth> {
        // Cap volontaire : À venir ≠ calendrier Accueil (horizon ~2 ans).
        val horizon = horizonMonths.coerceIn(1, MonthNavigation.UPCOMING_PREVIEW_MONTHS)
        val expenseTemplates = db.templateDao().activeExpenseTemplates()
        val incomeTemplates = db.templateDao().activeIncomeTemplates()
        val result = mutableListOf<UpcomingMonth>()
        for (offset in 1..horizon) {
            val ym = from.plusMonths(offset.toLong())
            val lines = mutableListOf<UpcomingLine>()
            // Revenus modèles = chaque mois (pas d’intervalle / ancre en schéma actuel).
            for (t in incomeTemplates) {
                lines += UpcomingLine(
                    label = t.label,
                    amountCents = t.amountCents,
                    dayOfMonth = t.dayOfMonth,
                    isIncome = true,
                    cadenceHint = "chaque mois",
                    isAgreement = false
                )
            }
            for (t in expenseTemplates) {
                val paid = if (DossierRemaining.isDossier(t.openingRemainingCents)) {
                    db.instanceDao().sumAmountCentsByTemplateAndStatus(
                        t.id,
                        ExpenseStatus.DONE
                    )
                } else {
                    0L
                }
                if (DossierRemaining.isSettled(t.openingRemainingCents, paid, t.addedFeesCents)) {
                    continue
                }
                val anchor = RecurrenceRules.parseAnchorOrNull(t.anchorYearMonth) ?: continue
                if (!RecurrenceRules.shouldMaterialize(anchor, t.intervalMonths, ym)) continue
                val amount = DossierRemaining.installmentCents(
                    t.amountCents,
                    t.openingRemainingCents,
                    paid,
                    t.addedFeesCents
                )
                if (amount <= 0L) continue
                lines += UpcomingLine(
                    label = t.label,
                    amountCents = amount,
                    dayOfMonth = t.dayOfMonth,
                    isIncome = false,
                    cadenceHint = cadenceHint(t.intervalMonths),
                    isAgreement = DossierRemaining.isDossier(t.openingRemainingCents)
                )
            }
            if (lines.isEmpty()) continue
            result += UpcomingMonth(
                yearMonth = ym,
                monthLabel = "${RecurrenceAnchor.frenchMonthLabel(ym.monthValue)} ${ym.year}",
                lines = lines.sortedWith(compareBy({ it.dayOfMonth }, { it.label }))
            )
        }
        return result
    }

    private fun cadenceHint(intervalMonths: Int): String = when (intervalMonths.coerceAtLeast(1)) {
        1 -> "chaque mois"
        3 -> "tous les 3 mois"
        6 -> "tous les 6 mois"
        12 -> "1× par an"
        else -> "tous les $intervalMonths mois"
    }
}
