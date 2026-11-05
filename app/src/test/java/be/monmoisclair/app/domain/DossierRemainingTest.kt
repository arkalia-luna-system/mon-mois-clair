package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.YearMonth

class DossierRemainingTest {

    private val from = YearMonth.of(2026, 10)

    @Test
    fun notADossier_whenOpeningNull() {
        assertThat(DossierRemaining.isDossier(null)).isFalse()
        assertThat(DossierRemaining.remainingCents(null, 5_000L)).isNull()
        assertThat(DossierRemaining.isSettled(null, 5_000L)).isFalse()
    }

    @Test
    fun remaining_subtractsActualDoneAmounts() {
        val rem = DossierRemaining.remainingCents(60_000L, 3_000L)
        assertThat(rem).isEqualTo(57_000L)
        assertThat(DossierRemaining.isSettled(60_000L, 3_000L)).isFalse()
    }

    @Test
    fun settled_whenPaidReachesOpening() {
        assertThat(DossierRemaining.isSettled(60_000L, 60_000L)).isTrue()
        assertThat(DossierRemaining.remainingCents(60_000L, 60_000L)).isEqualTo(0L)
        assertThat(DossierRemaining.isSettled(60_000L, 61_000L)).isTrue()
    }

    @Test
    fun installment_capsAtRemainingWhenBelowForfait() {
        assertThat(DossierRemaining.installmentCents(5_000L, 60_000L, 58_000L))
            .isEqualTo(2_000L)
        assertThat(DossierRemaining.installmentCents(5_000L, 60_000L, 10_000L))
            .isEqualTo(5_000L)
        assertThat(DossierRemaining.installmentCents(5_000L, null, 0L)).isEqualTo(5_000L)
    }

    @Test
    fun installment_reservesEarlierTodoBeforeNextMonth() {
        // Reste 7 000, forfait 5 000, mois courant TODO 5 000 → suivant = 2 000.
        assertThat(
            DossierRemaining.installmentCents(
                forfaitCents = 5_000L,
                openingRemainingCents = 12_000L,
                paidDoneCents = 5_000L,
                reservedTodoCents = 5_000L
            )
        ).isEqualTo(2_000L)
        assertThat(
            DossierRemaining.installmentCents(
                forfaitCents = 5_000L,
                openingRemainingCents = 12_000L,
                paidDoneCents = 5_000L,
                reservedTodoCents = 7_000L
            )
        ).isEqualTo(0L)
    }

    @Test
    fun pacePhrase_monthly_usesSi_andProjectedDate() {
        val steady = DossierRemaining.pacePhrase(
            forfaitCents = 5_000L,
            remainingCents = 45_000L,
            doneAmounts = listOf(5_000L, 5_000L),
            fromYearMonth = from
        )
        assertThat(steady).startsWith("Encore environ 9 mois si le versement reste de 50 €.")
        assertThat(steady).contains("Vers juin 2027.")
        assertThat(steady).doesNotContain("fin prévue")
        assertThat(steady).doesNotContain("recalculée")
    }

    @Test
    fun pacePhrase_recalculatesWhenAMonthDiverged() {
        // Reste 450 € après un FAIT partiel : toujours estimer + mention recalcul.
        val diverged = DossierRemaining.pacePhrase(
            forfaitCents = 5_000L,
            remainingCents = 45_000L,
            doneAmounts = listOf(5_000L, 3_000L),
            fromYearMonth = from
        )
        assertThat(diverged).isNotNull()
        assertThat(diverged).contains("si le versement reste de 50 €")
        assertThat(diverged).contains("Vers juin 2027.")
        assertThat(diverged).contains("recalculée")
    }

    @Test
    fun pacePhrase_lastSlice_andNonMonthly() {
        assertThat(
            DossierRemaining.pacePhrase(
                forfaitCents = 5_000L,
                remainingCents = 2_000L,
                fromYearMonth = from
            )
        ).startsWith("Encore environ 1 mois si le prochain versement est le reste, 20 €.")
        assertThat(
            DossierRemaining.pacePhrase(
                forfaitCents = 5_000L,
                remainingCents = 2_000L,
                fromYearMonth = from
            )
        )!!.contains("si")
        assertThat(
            DossierRemaining.pacePhrase(
                forfaitCents = 5_000L,
                remainingCents = 45_000L,
                doneAmounts = listOf(5_000L),
                fromYearMonth = from
            )
        ).contains("9 mois")
        assertThat(
            DossierRemaining.pacePhrase(
                forfaitCents = 5_000L,
                remainingCents = 10_000L,
                intervalMonths = 3,
                fromYearMonth = from
            )
        ).contains("2 versements")
        assertThat(
            DossierRemaining.pacePhrase(
                forfaitCents = 5_000L,
                remainingCents = 10_000L,
                doneAmounts = listOf(5_000L),
                fromYearMonth = from
            )
        ).contains("2 mois")
        assertThat(DossierRemaining.cadenceSuffix(12)).isEqualTo("/ an")
        assertThat(DossierRemaining.cadenceSuffix(1)).isEqualTo("/ mois")
        assertThat(DossierRemaining.cadenceSuffix(6)).isEqualTo("/ semestre")
        assertThat(DossierRemaining.cadenceSuffix(2)).isEqualTo("/ 2 mois")
        assertThat(DossierRemaining.pacePhrase(5_000L, 0L, fromYearMonth = from)).isNull()
    }

    @Test
    fun projectedEnd_nineMonthlyPaymentsFromOctober() {
        // 450 € / 50 € = 9 → oct + 8 = juin 2027
        assertThat(
            DossierRemaining.projectedEndYearMonth(45_000L, 5_000L, 1, from)
        ).isEqualTo(YearMonth.of(2027, 6))
    }

    @Test
    fun fees_increaseRemaining_withoutErasingDone() {
        val rem = DossierRemaining.remainingCents(60_000L, 5_000L, addedFeesCents = 2_000L)
        assertThat(rem).isEqualTo(57_000L)
    }
}
