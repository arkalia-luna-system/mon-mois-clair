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
}
