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
    fun stickyTokens_aggressiveOnTallAndCompact() {
        // 7.07 pack B : Ultra (>800) = mêmes densités sticky que S26.
        val compact = DisplayFit.from(360, 780)
        val tall = DisplayFit.from(412, 900)
        assertThat(compact.stickyPaddingVertical.value).isEqualTo(6f)
        assertThat(tall.stickyPaddingVertical.value).isEqualTo(6f)
        assertThat(compact.sectionGap.value).isEqualTo(2f)
        assertThat(tall.sectionGap.value).isEqualTo(2f)
        assertThat(compact.remainingAmountSp).isEqualTo(34f)
        assertThat(tall.remainingAmountSp).isEqualTo(34f)
        assertThat(compact.bannerPadding.value)
            .isLessThan(tall.bannerPadding.value)
    }

    @Test
    fun withFontScale_capsVisualHero() {
        val base = DisplayFit.from(360, 780)
        val at125 = base.withFontScale(1.25f)
        // Taille visuelle ≈ remainingAmountSp * fontScale ≤ 34.
        assertThat(at125.remainingAmountSp * 1.25f).isAtMost(34.01f)
        val at15 = base.withFontScale(1.5f)
        assertThat(at15.remainingAmountSp * 1.5f).isAtMost(30.01f)
        val at18 = base.withFontScale(1.8f)
        assertThat(at18.remainingAmountSp * 1.8f).isAtMost(28.01f)
        assertThat(base.withFontScale(1.0f).remainingAmountSp)
            .isEqualTo(base.remainingAmountSp)
    }
}
