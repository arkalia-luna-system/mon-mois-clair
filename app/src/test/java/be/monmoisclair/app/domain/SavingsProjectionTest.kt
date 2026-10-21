package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SavingsProjectionTest {

    @Test
    fun newLine_reducesRemaining() {
        // Salaire 1000, loyer 800 déjà dans le reste (200). Épargne 50 → 150.
        assertThat(SavingsProjection.remainingAfter(20_000L, 0L, 5_000L)).isEqualTo(15_000L)
    }

    @Test
    fun raisingExistingTodo_onlyAppliesTheDelta() {
        // Reste 150 avec une ligne TODO de 50. On passe à 80 → 120.
        assertThat(SavingsProjection.remainingAfter(15_000L, 5_000L, 8_000L)).isEqualTo(12_000L)
    }

    @Test
    fun sameTodoAndNew_leavesRemaining() {
        // Mois déjà FAIT : l’appelant passe le même montant des deux côtés (delta 0).
        assertThat(SavingsProjection.remainingAfter(15_000L, 8_000L, 8_000L)).isEqualTo(15_000L)
    }

    @Test
    fun tooHigh_goesNegative() {
        assertThat(SavingsProjection.remainingAfter(10_000L, 0L, 15_000L)).isEqualTo(-5_000L)
    }
}
