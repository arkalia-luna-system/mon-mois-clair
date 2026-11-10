package be.monmoisclair.app.ui.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.YearMonth

class HomeMonthRollTest {

    @Test
    fun keepsUserNavigationDuringEnsure() {
        val before = YearMonth.of(2026, 10)
        val calendar = YearMonth.of(2026, 11)
        val userWentTo = YearMonth.of(2026, 8)
        assertThat(
            HomeMonthRoll.monthAfterRollIfUnchanged(before, userWentTo, calendar)
        ).isEqualTo(userWentTo)
    }

    @Test
    fun rollsWhenStillOnOldCurrent() {
        val before = YearMonth.of(2026, 10)
        val calendar = YearMonth.of(2026, 11)
        assertThat(
            HomeMonthRoll.monthAfterRollIfUnchanged(before, before, calendar)
        ).isEqualTo(calendar)
    }
}
