package be.monmoisclair.app.ui.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StickyCoursesLabelTest {

    @Test
    fun future_preview_hides_line() {
        assertThat(
            stickyCoursesLabel(4_500L, 40_000L, futurePreview = true) { "45 €" }
        ).isNull()
    }

    @Test
    fun zero_shows_entry_point() {
        assertThat(stickyCoursesLabel(0L, 50_000L)).isEqualTo("Courses")
    }

    @Test
    fun spent_shows_amount_and_after() {
        assertThat(
            stickyCoursesLabel(4_500L, 40_500L) { cents -> "${cents / 100} €" }
        ).isEqualTo("Courses 45 € · il reste 405 €")
    }

    @Test
    fun chip_shows_amount_or_courses() {
        assertThat(stickyCoursesChipLabel(0L)).isEqualTo("Courses")
        assertThat(
            stickyCoursesChipLabel(4_500L) { cents -> "${cents / 100} €" }
        ).isEqualTo("45 €")
        assertThat(stickyCoursesChipLabel(1L, futurePreview = true)).isNull()
    }

    // --- Sheet status line (7.38) ---

    @Test
    fun sheet_zero_shows_noCoursesYet() {
        assertThat(coursesSheetStatusLine(0L, 50_000L))
            .isEqualTo("Aucune course notée pour l’instant")
    }

    @Test
    fun sheet_spent_positive_remaining_shows_il_reste() {
        assertThat(
            coursesSheetStatusLine(4_500L, 40_500L) { c -> "${c / 100} €" }
        ).isEqualTo("Noté 45 € · il reste 405 €")
    }

    @Test
    fun sheet_spent_negative_remaining_shows_il_reste() {
        assertThat(
            coursesSheetStatusLine(4_500L, -500L) { c -> "${c / 100} €" }
        ).isEqualTo("Noté 45 € · il reste -5 €")
    }
}
