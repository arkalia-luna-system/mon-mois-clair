package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.EuroFormat
import be.monmoisclair.app.domain.MonthSummaryCalculator
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lot F — texte brut à montrer ou envoyer (médiation / créancier).
 * Pas de PDF, pas de chemin fichier, pas de total « dettes » en titre.
 * « Mettre de côté » est à part, pas sous les dossiers.
 */
@Singleton
class PrepareMonthSummaryUseCase @Inject constructor(
    private val instanceDao: InstanceDao,
    private val templateDao: TemplateDao,
    private val appMetaDao: AppMetaDao,
    private val listDossiers: ListDossiersUseCase
) {
    data class Result(
        val text: String,
        /** true si le mois est en déficit (Reste < 0). */
        val isDeficit: Boolean
    )

    suspend operator fun invoke(yearMonth: YearMonth = AppClock.currentYearMonth()): Result {
        val ledgerId = AppClock.ledgerId(yearMonth)
        val incomes = instanceDao.getAllIncomes().filter { it.ledgerId == ledgerId }
            .sortedWith(compareBy({ it.dayOfMonth }, { it.label }))
        val expenses = instanceDao.getAllExpenses().filter { it.ledgerId == ledgerId }
            .sortedWith(compareBy({ it.dayOfMonth }, { it.label }))
        val summary = MonthSummaryCalculator.compute(incomes, expenses)
        val savingsId = appMetaDao.get(AppMetaKeys.SAVINGS_TEMPLATE_ID)?.value?.trim().orEmpty()
        val dossiers = listDossiers()
        val templates = templateDao.getAllExpenseTemplates().associateBy { it.id }

        val sb = StringBuilder()
        sb.appendLine("Mon Mois Clair — résumé")
        sb.appendLine(monthTitle(yearMonth))
        sb.appendLine()

        if (summary.remainingCents < 0L) {
            sb.appendLine(
                "Déficit ce mois : ${EuroFormat.formatCents(kotlin.math.abs(summary.remainingCents))}"
            )
            sb.appendLine("Reste après factures : ${EuroFormat.formatCents(summary.remainingCents)}")
            sb.appendLine()
        }

        sb.appendLine("Revenus")
        if (incomes.isEmpty()) {
            sb.appendLine("— aucun")
        } else {
            for (inc in incomes) {
                sb.appendLine(
                    "• ${inc.label} — ${EuroFormat.formatCents(inc.amountCents)}"
                )
            }
        }
        sb.appendLine()

        sb.appendLine("Factures du mois")
        val ordinary = expenses.filter { exp ->
            val tid = exp.templateId
            tid == null || tid != savingsId
        }
        if (ordinary.isEmpty()) {
            sb.appendLine("— aucune")
        } else {
            for (exp in ordinary) {
                val status = if (exp.status == ExpenseStatus.DONE) "FAIT" else "à payer"
                sb.appendLine(
                    "• ${exp.label} — ${EuroFormat.formatCents(exp.amountCents)} ($status)"
                )
            }
        }
        sb.appendLine()

        sb.appendLine("Dossiers (accords)")
        if (dossiers.items.isEmpty()) {
            sb.appendLine("— aucun")
        } else {
            for (d in dossiers.items) {
                val tpl = templates[d.templateId]
                val ref = tpl?.paymentNote?.trim().orEmpty()
                sb.append("• À qui : ${d.who}")
                if (d.what.isNotBlank()) sb.append(" · Pour quoi : ${d.what}")
                sb.appendLine()
                sb.appendLine(
                    "  Versement : ${EuroFormat.formatCents(d.monthCents)} · " +
                        "Reste : ${EuroFormat.formatCents(d.remainingCents)}"
                )
                if (ref.isNotBlank()) {
                    sb.appendLine("  Référence : $ref")
                }
            }
        }
        sb.appendLine()

        sb.appendLine("Mis de côté")
        val savingsLines = expenses.filter { it.templateId != null && it.templateId == savingsId }
        if (savingsLines.isEmpty()) {
            sb.appendLine("— aucun")
        } else {
            for (exp in savingsLines) {
                val status = if (exp.status == ExpenseStatus.DONE) "FAIT" else "à payer"
                sb.appendLine(
                    "• ${exp.label} — ${EuroFormat.formatCents(exp.amountCents)} ($status)"
                )
            }
            sb.appendLine(
                "(Virement vers un autre compte — pas de l’argent gardé dans l’application.)"
            )
        }
        sb.appendLine()
        sb.appendLine("Les photos des courriers sont dans l’application.")

        return Result(
            text = sb.toString().trimEnd() + "\n",
            isDeficit = summary.remainingCents < 0L
        )
    }

    private fun monthTitle(ym: YearMonth): String {
        val raw = ym.atDay(1).format(MONTH_TITLE)
        return raw.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString()
        }
    }

    companion object {
        private val MONTH_TITLE: DateTimeFormatter =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)
    }
}
