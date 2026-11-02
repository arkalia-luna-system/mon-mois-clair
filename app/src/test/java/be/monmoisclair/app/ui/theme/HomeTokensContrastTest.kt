package be.monmoisclair.app.ui.theme

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Contrastes Accueil 7.09 ([HomeTokens]) — cible familiale ≥ 7:1. */
class HomeTokensContrastTest {

    @Test
    fun day_inkAndStatus_meetAaaOnCanvasAndCard() {
        val d = HomeTokens.Day
        for (surface in listOf(d.canvas, d.card, d.heroSurface, d.chip)) {
            assertThat(WcagContrast.ratio(d.ink, surface)).isAtLeast(7.0)
            assertThat(WcagContrast.ratio(d.inkMuted, surface)).isAtLeast(7.0)
        }
        assertThat(WcagContrast.ratio(d.link, d.canvas)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(d.link, d.card)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(d.dangerInk, d.canvas)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(d.okInk, d.canvas)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(d.warn, d.canvas)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(A11yTokens.Day.buttonText, d.okFill)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(A11yTokens.Day.buttonText, d.dangerFill)).isAtLeast(7.0)
    }

    @Test
    fun night_inkAndStatus_meetAaaOnSurfaces() {
        val n = HomeTokens.Night
        val surfaces = listOf(n.canvas, n.card, n.heroSurface, n.chip)
        val inks = listOf(n.ink, n.inkMuted, n.link, n.dangerInk, n.okInk, n.warn)
        for (surface in surfaces) {
            for (ink in inks) {
                assertThat(WcagContrast.ratio(ink, surface)).isAtLeast(7.0)
            }
        }
        assertThat(WcagContrast.ratio(A11yTokens.Night.buttonText, n.okFill)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(A11yTokens.Night.buttonText, n.dangerFill)).isAtLeast(7.0)
    }
}
