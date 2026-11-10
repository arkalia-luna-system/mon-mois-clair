package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CarnetListLimitsTest {

    @Test
    fun truncate_underLimit_unchanged() {
        val r = CarnetListLimits.truncate(listOf(1, 2, 3), maxVisible = 10)
        assertThat(r.visible).containsExactly(1, 2, 3).inOrder()
        assertThat(r.hiddenCount).isEqualTo(0)
    }

    @Test
    fun truncate_overLimit_capsAndCounts() {
        val r = CarnetListLimits.truncate((1..405).toList(), CarnetListLimits.MAX_HOME_TIMELINE_ITEMS)
        assertThat(r.visible).hasSize(CarnetListLimits.MAX_HOME_TIMELINE_ITEMS)
        assertThat(r.hiddenCount).isEqualTo(5)
        assertThat(CarnetListLimits.hiddenListHint(r.hiddenCount)).contains("5")
    }

    @Test
    fun hiddenDossiersHint_singular() {
        assertThat(CarnetListLimits.hiddenDossiersHint(1)).contains("1 dossier")
    }
}
