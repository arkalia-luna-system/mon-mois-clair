package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.YearMonth

class MonthNavigationTest {

    @Test
    fun canGoPrev_alwaysTrue() {
        assertThat(MonthNavigation.canGoPrev()).isTrue()
    }

    @Test
    fun canGoNext_onlyTowardCurrent_notFuture() {
        val current = YearMonth.of(2026, 10)
        assertThat(MonthNavigation.canGoNext(YearMonth.of(2026, 9), current)).isTrue()
        assertThat(MonthNavigation.canGoNext(YearMonth.of(2026, 10), current)).isFalse()
        assertThat(MonthNavigation.canGoNext(YearMonth.of(2026, 11), current)).isFalse()
    }

    @Test
    fun canSelect_allowsPastAndCurrent_notFuture() {
        val current = YearMonth.of(2026, 10)
        assertThat(MonthNavigation.canSelect(YearMonth.of(2026, 10), current)).isTrue()
        assertThat(MonthNavigation.canSelect(YearMonth.of(2025, 1), current)).isTrue()
        assertThat(MonthNavigation.canSelect(YearMonth.of(2026, 11), current)).isFalse()
    }
}
