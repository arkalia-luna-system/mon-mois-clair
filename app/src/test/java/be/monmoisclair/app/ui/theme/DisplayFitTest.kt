package be.monmoisclair.app.ui.theme

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DisplayFitTest {

    @Test
    fun s26_like_height_uses_compact_chrome() {
        // S26 FHD+ ~1080×2340 @450dpi ≈ 384×832 ; @480dpi ≈ 360×780
        val fit = DisplayFit.from(screenWidthDp = 360, screenHeightDp = 780)
        assertThat(fit.compactHomeChrome).isTrue()
        assertThat(fit.remainingAmountSp).isEqualTo(28f)
    }

    @Test
    fun ultra_comfortable_height_keeps_roomy_chrome() {
        // Ultra QHD @560dpi ≈ 411×891
        val fit = DisplayFit.from(screenWidthDp = 411, screenHeightDp = 891)
        assertThat(fit.compactHomeChrome).isFalse()
        assertThat(fit.remainingAmountSp).isEqualTo(34f)
    }

    @Test
    fun ultra_with_large_display_scale_becomes_compact() {
        // S25 Ultra 1440×3120 @640dpi → 360×780 (réglage One UI « taille d’affichage »)
        val fit = DisplayFit.from(screenWidthDp = 360, screenHeightDp = 780)
        assertThat(fit.compactHomeChrome).isTrue()
    }

    @Test
    fun side_padding_respects_one_ui_24dp() {
        val fit = DisplayFit.from(384, 832)
        assertThat(fit.screenPadding).isEqualTo(A11yTokens.screenPadding)
        assertThat(fit.stickyPaddingHorizontal).isEqualTo(A11yTokens.screenPadding)
    }
}
