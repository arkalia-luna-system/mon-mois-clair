package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DossierRemainingTest {

    @Test
    fun notADossier_whenOpeningNull() {
        assertThat(DossierRemaining.isDossier(null)).isFalse()
        assertThat(DossierRemaining.remainingCents(null, 5_000L)).isNull()
        assertThat(DossierRemaining.isSettled(null, 5_000L)).isFalse()
    }

    @Test
    fun remaining_subtractsActualDoneAmounts() {
        // 600 € départ, un mois à 30 € FAIT → 570 € (pas 550).
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
        // Forfait 50 €, reste 20 € → dernière ligne partielle.
        assertThat(DossierRemaining.installmentCents(5_000L, 60_000L, 58_000L))
            .isEqualTo(2_000L)
        // Reste encore > forfait → forfait intact.
        assertThat(DossierRemaining.installmentCents(5_000L, 60_000L, 10_000L))
            .isEqualTo(5_000L)
        // Pas un dossier → forfait.
        assertThat(DossierRemaining.installmentCents(5_000L, null, 0L)).isEqualTo(5_000L)
    }

    @Test
    fun pacePhrase_monthly_usesSi_andHidesWhenAMonthDiverged() {
        val steady = DossierRemaining.pacePhrase(
            forfaitCents = 5_000L,
            remainingCents = 45_000L,
            doneAmounts = listOf(5_000L, 5_000L),
            todoAmounts = listOf(5_000L)
        )
        assertThat(steady).isEqualTo("Encore environ 9 mois si le versement reste de 50 €.")
        assertThat(steady).doesNotContain("fin prévue")
        val diverged = DossierRemaining.pacePhrase(
            forfaitCents = 5_000L,
            remainingCents = 45_000L,
            doneAmounts = listOf(5_000L, 3_000L)
        )
        assertThat(diverged).isNull()
    }

    @Test
    fun pacePhrase_lastSlice_andNonMonthly() {
        assertThat(
            DossierRemaining.pacePhrase(
                forfaitCents = 5_000L,
                remainingCents = 2_000L,
                todoAmounts = listOf(2_000L)
            )
        ).isEqualTo("Encore environ 1 mois si le prochain versement est le reste, 20 €.")
        assertThat(
            DossierRemaining.pacePhrase(
                forfaitCents = 5_000L,
                remainingCents = 2_000L
            )
        )!!.contains("si")
        // TODO d’un autre montant (édition manuelle) : la phrase reste — seuls les FAIT comptent.
        assertThat(
            DossierRemaining.pacePhrase(
                forfaitCents = 5_000L,
                remainingCents = 45_000L,
                doneAmounts = listOf(5_000L),
                todoAmounts = listOf(4_000L)
            )
        ).isEqualTo("Encore environ 9 mois si le versement reste de 50 €.")
        assertThat(
            DossierRemaining.pacePhrase(
                forfaitCents = 5_000L,
                remainingCents = 10_000L,
                intervalMonths = 3
            )
        ).isEqualTo("Encore environ 2 versements si le versement reste de 50 €.")
        // Division exacte : toujours « environ » (frais possibles).
        assertThat(
            DossierRemaining.pacePhrase(
                forfaitCents = 5_000L,
                remainingCents = 10_000L,
                doneAmounts = listOf(5_000L)
            )
        ).isEqualTo("Encore environ 2 mois si le versement reste de 50 €.")
        assertThat(DossierRemaining.cadenceSuffix(12)).isEqualTo("/ an")
        assertThat(DossierRemaining.cadenceSuffix(1)).isEqualTo("/ mois")
        assertThat(DossierRemaining.cadenceSuffix(6)).isEqualTo("/ semestre")
        assertThat(DossierRemaining.cadenceSuffix(2)).isEqualTo("/ 2 mois")
        assertThat(DossierRemaining.pacePhrase(5_000L, 0L)).isNull()
    }

    @Test
    fun fees_increaseRemaining_withoutErasingDone() {
        // 600 € − 50 € FAIT + 20 € frais → 570 €
        val rem = DossierRemaining.remainingCents(60_000L, 5_000L, addedFeesCents = 2_000L)
        assertThat(rem).isEqualTo(57_000L)
    }
}
