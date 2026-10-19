package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.YearMonth

class RecurrenceAnchorTest {

    @Test
    fun same_month_keeps_year() {
        assertThat(
            RecurrenceAnchor.nextYearMonth(10, YearMonth.of(2026, 10))
        ).isEqualTo(YearMonth.of(2026, 10))
    }

    @Test
    fun past_month_rolls_to_next_year() {
        // En octobre, « mars » → mars de l’année suivante
        assertThat(
            RecurrenceAnchor.nextYearMonth(3, YearMonth.of(2026, 10))
        ).isEqualTo(YearMonth.of(2027, 3))
    }

    @Test
    fun future_month_same_year() {
        assertThat(
            RecurrenceAnchor.nextYearMonth(11, YearMonth.of(2026, 10))
        ).isEqualTo(YearMonth.of(2026, 11))
    }
}
