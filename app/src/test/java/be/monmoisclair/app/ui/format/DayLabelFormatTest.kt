package be.monmoisclair.app.ui.format

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.YearMonth

class DayLabelFormatTest {

    @Test
    fun monthTitle_frenchCapitalized() {
        assertThat(DayLabelFormat.monthTitle(YearMonth.of(2026, 10))).isEqualTo("Octobre 2026")
        assertThat(DayLabelFormat.monthTitle(YearMonth.of(2026, 2))).isEqualTo("Février 2026")
        assertThat(DayLabelFormat.monthTitle(YearMonth.of(2025, 12))).isEqualTo("Décembre 2025")
    }

    @Test
    fun dayOfMonthLabel() {
        assertThat(DayLabelFormat.dayOfMonthLabel(1)).isEqualTo("Le 1")
        assertThat(DayLabelFormat.dayOfMonthLabel(31)).isEqualTo("Le 31")
    }
}
