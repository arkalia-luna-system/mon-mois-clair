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
}
