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
    fun fees_increaseRemaining_withoutErasingDone() {
        // 600 € − 50 € FAIT + 20 € frais → 570 €
        val rem = DossierRemaining.remainingCents(60_000L, 5_000L, addedFeesCents = 2_000L)
        assertThat(rem).isEqualTo(57_000L)
    }
}
