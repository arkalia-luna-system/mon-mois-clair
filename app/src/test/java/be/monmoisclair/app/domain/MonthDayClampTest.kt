package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.YearMonth

class MonthDayClampTest {

    @Test
    fun clamp_31_in_february_nonLeap_returns_28() {
        val result = MonthDayClamp.clamp(31, YearMonth.of(2025, 2))
        assertThat(result).isEqualTo(28)
    }

    @Test
    fun clamp_31_in_february_leap_returns_29() {
        val result = MonthDayClamp.clamp(31, YearMonth.of(2024, 2))
        assertThat(result).isEqualTo(29)
    }

    @Test
    fun clamp_31_in_april_returns_30() {
        val result = MonthDayClamp.clamp(31, YearMonth.of(2026, 4))
        assertThat(result).isEqualTo(30)
    }

    @Test
    fun clamp_31_in_january_returns_31() {
        val result = MonthDayClamp.clamp(31, YearMonth.of(2026, 1))
        assertThat(result).isEqualTo(31)
    }

    @Test
    fun clamp_5_unchanged() {
        val result = MonthDayClamp.clamp(5, YearMonth.of(2026, 2))
        assertThat(result).isEqualTo(5)
    }

    @Test
    fun clamp_rejects_day_0() {
        assertThrows(IllegalArgumentException::class.java) {
            MonthDayClamp.clamp(0, YearMonth.of(2026, 1))
        }
    }

    @Test
    fun clamp_rejects_day_32() {
        assertThrows(IllegalArgumentException::class.java) {
            MonthDayClamp.clamp(32, YearMonth.of(2026, 1))
        }
    }
}
