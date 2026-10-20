package be.monmoisclair.app.domain

import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.domain.model.ExpenseStatus

data class MonthSummary(
    val totalIncomeCents: Long,
    val totalExpensesCents: Long,
    val remainingCents: Long,
    val stillToPayCents: Long
)

object MonthSummaryCalculator {

    /**
     * `remainingCents` = revenus − **toutes** les factures (FAIT ou non).
     * C’est le « Reste après factures » : on réserve aussi ce qui n’est pas encore payé.
     * Les divers (courses) **ne** sont **pas** dedans — indicatif à part (`après divers`).
     * `stillToPayCents` = somme des TODO uniquement (ligne « À payer »).
     */
    fun compute(
        incomes: List<IncomeInstanceEntity>,
        expenses: List<ExpenseInstanceEntity>
    ): MonthSummary {
        val totalIncome = incomes.sumOf { it.amountCents }
        val totalExpenses = expenses.sumOf { it.amountCents }
        val stillToPay = expenses
            .asSequence()
            .filter { it.status == ExpenseStatus.TODO }
            .sumOf { it.amountCents }
        return MonthSummary(
            totalIncomeCents = totalIncome,
            totalExpensesCents = totalExpenses,
            remainingCents = totalIncome - totalExpenses,
            stillToPayCents = stillToPay
        )
    }
}
