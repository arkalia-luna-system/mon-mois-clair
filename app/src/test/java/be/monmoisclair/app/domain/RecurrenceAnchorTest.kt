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

    @Test
    fun fromPastLedger_keepsSameYearWhenMonthMatches() {
        // Ajout depuis mars 2026 affiché : ancre « mars » → 2026-03 (pas 2027)
        assertThat(
            RecurrenceAnchor.nextYearMonth(3, YearMonth.of(2026, 3))
        ).isEqualTo(YearMonth.of(2026, 3))
    }

    @Test
    fun resolveTemplateAnchor_keepsYear_whenMonthUnchanged() {
        // Mai 2026, ancre janv. 2026 : corriger le nom ne doit pas → janv. 2027 (P3-F01).
        val now = YearMonth.of(2026, 5)
        val existing = YearMonth.of(2026, 1)
        val kept = RecurrenceAnchor.resolveTemplateAnchor(
            selectedMonth = 1,
            existingAnchor = existing,
            now = now
        )
        assertThat(kept).isEqualTo(existing)
        assertThat(
            RecurrenceRules.shouldMaterialize(kept, 3, YearMonth.of(2026, 7))
        ).isTrue()
    }

    @Test
    fun resolveTemplateAnchor_advancesFromExisting_whenMonthChanges() {
        val now = YearMonth.of(2026, 5)
        val existing = YearMonth.of(2026, 1)
        assertThat(
            RecurrenceAnchor.resolveTemplateAnchor(
                selectedMonth = 7,
                existingAnchor = existing,
                now = now
            )
        ).isEqualTo(YearMonth.of(2026, 7))
    }

    @Test
    fun resolveTemplateAnchor_changedMonth_doesNotLandBeforeNow() {
        val moved = RecurrenceAnchor.resolveTemplateAnchor(
            selectedMonth = 3,
            existingAnchor = YearMonth.of(2026, 1),
            now = YearMonth.of(2026, 5)
        )
        assertThat(moved.isBefore(YearMonth.of(2026, 5))).isFalse()
        assertThat(moved.monthValue).isEqualTo(3)
    }
}
