package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.YearMonth

class RecurrenceRulesTest {

    @Test
    fun monthly_every_month() {
        val a = YearMonth.of(2026, 1)
        assertThat(RecurrenceRules.shouldMaterialize(a, 1, YearMonth.of(2026, 1))).isTrue()
        assertThat(RecurrenceRules.shouldMaterialize(a, 1, YearMonth.of(2026, 2))).isTrue()
    }

    @Test
    fun quarterly() {
        val a = YearMonth.of(2026, 1)
        assertThat(RecurrenceRules.shouldMaterialize(a, 3, YearMonth.of(2026, 1))).isTrue()
        assertThat(RecurrenceRules.shouldMaterialize(a, 3, YearMonth.of(2026, 2))).isFalse()
        assertThat(RecurrenceRules.shouldMaterialize(a, 3, YearMonth.of(2026, 4))).isTrue()
        assertThat(RecurrenceRules.shouldMaterialize(a, 3, YearMonth.of(2026, 7))).isTrue()
    }

    @Test
    fun before_anchor_false() {
        assertThat(
            RecurrenceRules.shouldMaterialize(
                YearMonth.of(2026, 6),
                1,
                YearMonth.of(2026, 5)
            )
        ).isFalse()
    }
}
