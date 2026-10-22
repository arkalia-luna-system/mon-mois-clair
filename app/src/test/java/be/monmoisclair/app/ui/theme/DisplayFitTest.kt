package be.monmoisclair.app.ui.theme

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DisplayFitTest {

    @Test
    fun compactHomeChrome_whenHeightAtMost800() {
        assertThat(DisplayFit.from(360, 780).compactHomeChrome).isTrue()
        assertThat(DisplayFit.from(360, 800).compactHomeChrome).isTrue()
        assertThat(DisplayFit.from(360, 801).compactHomeChrome).isFalse()
    }

    @Test
    fun remainingAmountSp_smallerOnCompact() {
        val compact = DisplayFit.from(360, 780)
        val tall = DisplayFit.from(412, 900)
        assertThat(compact.remainingAmountSp).isLessThan(tall.remainingAmountSp)
        // Charte : compact ≥ 34 sp ; confort ~36–42.
        assertThat(compact.remainingAmountSp).isAtLeast(34f)
        assertThat(tall.remainingAmountSp).isAtLeast(36f)
        assertThat(tall.remainingAmountSp).isAtMost(42f)
    }

    @Test
    fun stickyPadding_tighterOnCompact() {
        val compact = DisplayFit.from(360, 780)
        val tall = DisplayFit.from(412, 900)
        assertThat(compact.stickyPaddingVertical.value)
            .isLessThan(tall.stickyPaddingVertical.value)
        assertThat(compact.bannerPadding.value)
            .isLessThan(tall.bannerPadding.value)
    }
}
