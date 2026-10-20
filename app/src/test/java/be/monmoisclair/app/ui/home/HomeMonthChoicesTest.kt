package be.monmoisclair.app.ui.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.YearMonth

class HomeMonthChoicesTest {

    @Test
    fun recentMonths_startsAtCurrent_neverFuture() {
        val current = YearMonth.of(2026, 10)
        val months = HomeMonthChoices.recentMonths(current, count = 12)
        assertThat(months).hasSize(12)
        assertThat(months.first()).isEqualTo(current)
        assertThat(months.last()).isEqualTo(YearMonth.of(2025, 11))
        assertThat(months).doesNotContain(YearMonth.of(2026, 11))
    }

    @Test
    fun canSelect_blocksFuture() {
        val current = YearMonth.of(2026, 10)
        assertThat(HomeMonthChoices.canSelect(current, current)).isTrue()
        assertThat(HomeMonthChoices.canSelect(current.minusMonths(1), current)).isTrue()
        assertThat(HomeMonthChoices.canSelect(current.plusMonths(1), current)).isFalse()
    }
}
