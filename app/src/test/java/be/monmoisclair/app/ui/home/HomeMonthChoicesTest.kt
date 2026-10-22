package be.monmoisclair.app.ui.home

import be.monmoisclair.app.domain.MonthNavigation
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.YearMonth

class HomeMonthChoicesTest {

    @Test
    fun recentMonths_startsWithFarFuture_thenCurrent() {
        val current = YearMonth.of(2026, 10)
        val months = HomeMonthChoices.recentMonths(current)
        assertThat(months).hasSize(
            MonthNavigation.FUTURE_MONTHS + MonthNavigation.HISTORY_MONTHS
        )
        assertThat(months.first()).isEqualTo(YearMonth.of(2028, 10))
        assertThat(months[MonthNavigation.FUTURE_MONTHS - 1]).isEqualTo(YearMonth.of(2026, 11))
        assertThat(months[MonthNavigation.FUTURE_MONTHS]).isEqualTo(current)
        assertThat(months.last()).isEqualTo(YearMonth.of(2024, 11))
        assertThat(months).contains(YearMonth.of(2027, 10))
    }

    @Test
    fun recentStrip_isChronologicalAndShort() {
        val current = YearMonth.of(2026, 10)
        val strip = HomeMonthChoices.recentStrip(current)
        assertThat(strip).containsExactly(
            YearMonth.of(2026, 8),
            YearMonth.of(2026, 9),
            YearMonth.of(2026, 10),
            YearMonth.of(2026, 11),
            YearMonth.of(2026, 12),
            YearMonth.of(2027, 1)
        ).inOrder()
        assertThat(strip).hasSize(
            HomeMonthChoices.RECENT_PAST + 1 + HomeMonthChoices.RECENT_STRIP_FUTURE
        )
    }

    @Test
    fun otherMonthsByYear_excludesRecentStrip() {
        val current = YearMonth.of(2026, 10)
        val recent = HomeMonthChoices.recentStrip(current)
        val other = HomeMonthChoices.otherMonthsByYear(current, recent)
        val flat = other.flatMap { it.second }
        assertThat(flat).containsNoneIn(recent)
        assertThat(flat).contains(YearMonth.of(2026, 7))
        assertThat(flat).contains(YearMonth.of(2025, 10))
        assertThat(other.map { it.first }).contains(2026)
        assertThat(other.map { it.first }).contains(2025)
        assertThat(other.map { it.first }).contains(2024)
        // 2028 n’a que des mois futurs hors bande courte → présents dans « autres »
        assertThat(other.map { it.first }).contains(2028)
    }

    @Test
    fun calendarByYear_coversHorizonAndSortsMonths() {
        val current = YearMonth.of(2026, 10)
        val cal = HomeMonthChoices.calendarByYear(current)
        assertThat(cal.map { it.first }.first()).isEqualTo(2028)
        assertThat(cal.map { it.first }.last()).isEqualTo(2024)
        val y2026 = cal.first { it.first == 2026 }.second
        assertThat(y2026.first().monthValue).isEqualTo(1)
        assertThat(y2026.last()).isEqualTo(YearMonth.of(2026, 12))
        assertThat(y2026).contains(current)
        val y2027 = cal.first { it.first == 2027 }.second
        assertThat(y2027).hasSize(12)
    }

    @Test
    fun canSelect_allowsYearPlusOne() {
        val current = YearMonth.of(2026, 10)
        assertThat(HomeMonthChoices.canSelect(current, current)).isTrue()
        assertThat(HomeMonthChoices.canSelect(current.minusMonths(1), current)).isTrue()
        assertThat(HomeMonthChoices.canSelect(current.plusMonths(12), current)).isTrue()
        assertThat(HomeMonthChoices.canSelect(current.plusMonths(24), current)).isTrue()
        assertThat(HomeMonthChoices.canSelect(current.plusMonths(25), current)).isFalse()
    }

    @Test
    fun yearsInOrder_groupsNewestYearFirst() {
        val current = YearMonth.of(2026, 10)
        val months = HomeMonthChoices.recentMonths(current)
        assertThat(HomeMonthChoices.yearsInOrder(months))
            .containsExactly(2028, 2027, 2026, 2025, 2024)
            .inOrder()
        assertThat(months.filter { it.year == 2025 }).isNotEmpty()
    }
}
