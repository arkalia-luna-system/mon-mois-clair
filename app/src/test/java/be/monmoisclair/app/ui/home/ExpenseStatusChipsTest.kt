package be.monmoisclair.app.ui.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExpenseStatusChipsTest {

    @Test
    fun overdue_agreement_showsRetardAndPace_notPrelevement() {
        val chips = ExpenseStatusChips.labels(
            isOverdue = true,
            isDone = false,
            doneLabel = null,
            isAutoDebit = true,
            isAgreement = true,
            hideAgreementChip = false,
            remainingPayments = 4,
            compact = false
        )
        assertThat(chips).containsExactly("En retard", "encore ~4").inOrder()
    }

    @Test
    fun agreement_autoDebit_prefersPaceAndAccord_beforePrelevement() {
        val chips = ExpenseStatusChips.labels(
            isOverdue = false,
            isDone = false,
            doneLabel = null,
            isAutoDebit = true,
            isAgreement = true,
            hideAgreementChip = false,
            remainingPayments = 3,
            compact = false
        )
        assertThat(chips).containsExactly("encore ~3", "Accord").inOrder()
        assertThat(chips).doesNotContain("Prélèvement")
    }

    @Test
    fun prelevement_onlyWhenSlotAvailable() {
        val chips = ExpenseStatusChips.labels(
            isOverdue = false,
            isDone = false,
            doneLabel = null,
            isAutoDebit = true,
            isAgreement = false,
            hideAgreementChip = false,
            remainingPayments = null,
            compact = false
        )
        assertThat(chips).containsExactly("Prélèvement")
    }

    @Test
    fun compact_onlyCritical() {
        val chips = ExpenseStatusChips.labels(
            isOverdue = true,
            isDone = false,
            doneLabel = null,
            isAutoDebit = true,
            isAgreement = true,
            hideAgreementChip = false,
            remainingPayments = 9,
            compact = true
        )
        assertThat(chips).containsExactly("En retard")
    }

    @Test
    fun maxTwoChips() {
        val chips = ExpenseStatusChips.labels(
            isOverdue = false,
            isDone = true,
            doneLabel = "FAIT le 01/11/26",
            isAutoDebit = true,
            isAgreement = true,
            hideAgreementChip = false,
            remainingPayments = 2,
            compact = false
        )
        assertThat(chips).hasSize(2)
        assertThat(chips[0]).startsWith("FAIT")
        assertThat(chips[1]).isEqualTo("encore ~2")
    }
}
