package be.monmoisclair.app.ui.components

import be.monmoisclair.app.ui.theme.A11yTokens
import be.monmoisclair.app.ui.theme.carnetTypography
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** PageTitle (InkMuted) ≠ HubRow (Ink) — modèle Menu. */
@RunWith(RobolectricTestRunner::class)
class CarnetTitleTokensTest {

    @Test
    fun pageTitle_isMuted_hubRow_isInk() {
        val day = A11yTokens.Day
        val night = A11yTokens.Night
        assertThat(CarnetTitleTokens.pageTitleColor(day)).isEqualTo(day.inkMuted)
        assertThat(CarnetTitleTokens.hubRowLabelColor(day)).isEqualTo(day.ink)
        assertThat(CarnetTitleTokens.pageTitleColor(day))
            .isNotEqualTo(CarnetTitleTokens.hubRowLabelColor(day))
        assertThat(CarnetTitleTokens.sectionTitleColor(day)).isEqualTo(day.inkMuted)
        assertThat(CarnetTitleTokens.pageTitleColor(night)).isEqualTo(night.inkMuted)
        assertThat(CarnetTitleTokens.hubRowLabelColor(night)).isEqualTo(night.ink)
    }

    @Test
    fun pageAndHub_shareTitleLarge_sectionIsTitleMedium() {
        val ty = carnetTypography(A11yTokens.Day.ink, A11yTokens.Day.inkMuted)
        assertThat(CarnetTitleTokens.pageTitleStyle(ty).fontSize)
            .isEqualTo(CarnetTitleTokens.hubRowLabelStyle(ty).fontSize)
        assertThat(CarnetTitleTokens.sectionTitleStyle(ty).fontSize)
            .isEqualTo(ty.titleMedium.fontSize)
        assertThat(CarnetTitleTokens.sectionTitleStyle(ty).fontSize.value)
            .isLessThan(CarnetTitleTokens.pageTitleStyle(ty).fontSize.value)
    }
}
