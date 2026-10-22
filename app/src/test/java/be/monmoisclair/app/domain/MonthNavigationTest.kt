package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.YearMonth

class MonthNavigationTest {

    @Test
    fun canGoPrev_stopsAtHorizon() {
        val current = YearMonth.of(2026, 10)
        assertThat(MonthNavigation.canGoPrev(current, current)).isTrue()
        val oldest = MonthNavigation.oldestSelectable(current)
        assertThat(MonthNavigation.canGoPrev(oldest.plusMonths(1), current)).isTrue()
        assertThat(MonthNavigation.canGoPrev(oldest, current)).isFalse()
    }

    @Test
    fun canGoNext_allowsFutureWithinHorizon() {
        val current = YearMonth.of(2026, 10)
        assertThat(MonthNavigation.canGoNext(YearMonth.of(2026, 9), current)).isTrue()
        assertThat(MonthNavigation.canGoNext(YearMonth.of(2026, 10), current)).isTrue()
        assertThat(MonthNavigation.canGoNext(YearMonth.of(2026, 11), current)).isTrue()
        assertThat(MonthNavigation.canGoNext(YearMonth.of(2026, 12), current)).isTrue()
        assertThat(MonthNavigation.canGoNext(YearMonth.of(2027, 1), current)).isFalse()
    }

    @Test
    fun canSelect_allowsPastCurrentAndNearFuture() {
        val current = YearMonth.of(2026, 10)
        assertThat(MonthNavigation.canSelect(YearMonth.of(2026, 10), current)).isTrue()
        assertThat(MonthNavigation.canSelect(YearMonth.of(2025, 1), current)).isTrue()
        assertThat(MonthNavigation.canSelect(YearMonth.of(2024, 1), current)).isFalse()
        assertThat(MonthNavigation.canSelect(YearMonth.of(2026, 11), current)).isTrue()
        assertThat(MonthNavigation.canSelect(YearMonth.of(2027, 1), current)).isTrue()
        assertThat(MonthNavigation.canSelect(YearMonth.of(2027, 2), current)).isFalse()
    }

    @Test
    fun isFuturePreview_onlyAfterCurrent() {
        val current = YearMonth.of(2026, 10)
        assertThat(MonthNavigation.isFuturePreview(current, current)).isFalse()
        assertThat(MonthNavigation.isFuturePreview(YearMonth.of(2026, 9), current)).isFalse()
        assertThat(MonthNavigation.isFuturePreview(YearMonth.of(2026, 11), current)).isTrue()
    }
}
