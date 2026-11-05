package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.MonthNavigation
import be.monmoisclair.app.domain.RecurrenceAnchor
import be.monmoisclair.app.domain.RecurrenceRules
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.Instant
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
 *
 * B6 : revenus filtrés par mois de création du modèle.
 * B8 : accords — poche restante après FAIT + TODO déjà posés (mois ≤ courant),
 * puis allocation séquentielle sur l’horizon (même sans mois futurs ouverts).
 */
@Singleton
class ListUpcomingMonthsUseCase @Inject constructor(
    private val db: AppDatabase
) {
    suspend operator fun invoke(
        from: YearMonth = AppClock.currentYearMonth(),
        horizonMonths: Int = MonthNavigation.UPCOMING_PREVIEW_MONTHS
    ): List<UpcomingMonth> {
        val horizon = horizonMonths.coerceIn(1, MonthNavigation.UPCOMING_PREVIEW_MONTHS)
        val expenseTemplates = db.templateDao().activeExpenseTemplates()
        val incomeTemplates = db.templateDao().activeIncomeTemplates()
        val fromLedger = from.toString()

        // Poche restante par dossier : après FAIT + TODO ≤ mois courant.
        val dossierPocket = mutableMapOf<String, Long>()
        for (t in expenseTemplates) {
            if (!DossierRemaining.isDossier(t.openingRemainingCents)) continue
            val paid = db.instanceDao().sumAmountCentsByTemplateAndStatus(
                t.id,
                ExpenseStatus.DONE
            )
            if (DossierRemaining.isSettled(t.openingRemainingCents, paid, t.addedFeesCents)) {
                dossierPocket[t.id] = 0L
                continue
            }
            val rem = DossierRemaining.remainingCents(
                t.openingRemainingCents,
                paid,
                t.addedFeesCents
            ) ?: continue
            val reservedTodo = db.instanceDao().expensesByTemplate(t.id)
                .asSequence()
                .filter {
                    it.status == ExpenseStatus.TODO && it.ledgerId <= fromLedger
                }
                .sumOf { it.amountCents }
            dossierPocket[t.id] = (rem - reservedTodo).coerceAtLeast(0L)
        }

        val result = mutableListOf<UpcomingMonth>()
        for (offset in 1..horizon) {
            val ym = from.plusMonths(offset.toLong())
            val lines = mutableListOf<UpcomingLine>()
            for (t in incomeTemplates) {
                val anchor = YearMonth.from(
                    Instant.ofEpochMilli(t.createdAtEpochMs)
                        .atZone(AppClock.zone)
                        .toLocalDate()
                )
                if (ym.isBefore(anchor)) continue
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
                val isDossier = DossierRemaining.isDossier(t.openingRemainingCents)
                if (isDossier && (dossierPocket[t.id] ?: 0L) <= 0L) continue
                val anchor = RecurrenceRules.parseAnchorOrNull(t.anchorYearMonth)
                    ?: YearMonth.from(
                        Instant.ofEpochMilli(t.createdAtEpochMs)
                            .atZone(AppClock.zone)
                            .toLocalDate()
                    )
                if (!RecurrenceRules.shouldMaterialize(anchor, t.intervalMonths, ym)) continue
                val amount = if (isDossier) {
                    val pocket = dossierPocket[t.id] ?: 0L
                    val capped = minOf(t.amountCents, pocket)
                    if (capped <= 0L) continue
                    dossierPocket[t.id] = pocket - capped
                    capped
                } else {
                    t.amountCents
                }
                lines += UpcomingLine(
                    label = t.label,
                    amountCents = amount,
                    dayOfMonth = t.dayOfMonth,
                    isIncome = false,
                    cadenceHint = cadenceHint(t.intervalMonths),
                    isAgreement = isDossier
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
