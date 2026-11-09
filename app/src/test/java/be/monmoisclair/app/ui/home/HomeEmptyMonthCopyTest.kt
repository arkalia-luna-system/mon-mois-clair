package be.monmoisclair.app.ui.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HomeEmptyMonthCopyTest {

    @Test
    fun pastMonth_titleAndCta() {
        assertThat(HomeEmptyMonthCopy.title(isPastMonth = true, isFuturePreview = false))
            .isEqualTo("Aucune ligne ce mois-là")
        assertThat(HomeEmptyMonthCopy.body(isPastMonth = true, isFuturePreview = false))
            .contains("mois en cours")
        assertThat(
            HomeEmptyMonthCopy.showGoCurrentCta(isPastMonth = true, isFuturePreview = false)
        ).isTrue()
    }

    @Test
    fun currentMonth_addCta() {
        assertThat(HomeEmptyMonthCopy.title(isPastMonth = false, isFuturePreview = false))
            .isEqualTo("Rien encore ce mois")
        assertThat(
            HomeEmptyMonthCopy.showGoCurrentCta(isPastMonth = false, isFuturePreview = false)
        ).isFalse()
    }

    @Test
    fun futurePreview_unchanged() {
        assertThat(HomeEmptyMonthCopy.title(isPastMonth = false, isFuturePreview = true))
            .isEqualTo("Rien de prévu ce mois-là")
        assertThat(
            HomeEmptyMonthCopy.showGoCurrentCta(isPastMonth = false, isFuturePreview = true)
        ).isTrue()
    }
}
