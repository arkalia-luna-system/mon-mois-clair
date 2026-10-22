package be.monmoisclair.app.ui.home

import be.monmoisclair.app.domain.MonthNavigation
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.YearMonth

class HomeMonthChoicesTest {

    @Test
    fun recentMonths_startsWithNearFuture_thenCurrent() {
        val current = YearMonth.of(2026, 10)
        val months = HomeMonthChoices.recentMonths(current)
        assertThat(months).hasSize(
            MonthNavigation.FUTURE_MONTHS + MonthNavigation.HISTORY_MONTHS
        )
        assertThat(months.first()).isEqualTo(YearMonth.of(2027, 1))
        assertThat(months[MonthNavigation.FUTURE_MONTHS - 1]).isEqualTo(YearMonth.of(2026, 11))
        assertThat(months[MonthNavigation.FUTURE_MONTHS]).isEqualTo(current)
        assertThat(months.last()).isEqualTo(YearMonth.of(2024, 11))
        assertThat(months).contains(YearMonth.of(2026, 11))
        assertThat(months).contains(YearMonth.of(2026, 12))
    }

    @Test
    fun canSelect_allowsNearFuture() {
        val current = YearMonth.of(2026, 10)
        assertThat(HomeMonthChoices.canSelect(current, current)).isTrue()
        assertThat(HomeMonthChoices.canSelect(current.minusMonths(1), current)).isTrue()
        assertThat(HomeMonthChoices.canSelect(current.plusMonths(1), current)).isTrue()
        assertThat(HomeMonthChoices.canSelect(current.plusMonths(4), current)).isFalse()
    }

    @Test
    fun yearsInOrder_groupsNewestYearFirst() {
        val current = YearMonth.of(2026, 10)
        val months = HomeMonthChoices.recentMonths(current)
        assertThat(HomeMonthChoices.yearsInOrder(months))
            .containsExactly(2027, 2026, 2025, 2024)
            .inOrder()
        assertThat(months.filter { it.year == 2025 }).isNotEmpty()
    }
}
