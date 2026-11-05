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
        // Badge Menu « ! » (dangerFill) + pastille Accord or champagne.
        assertThat(WcagContrast.ratio(A11yTokens.Day.buttonText, d.dangerFill)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(A11yTokens.Day.onGold, A11yTokens.Day.accentAuto)).isAtLeast(7.0)
        // Wells Courses / Épargne + code backup (or sur carte).
        assertThat(WcagContrast.ratio(d.ink, d.coursesChip)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(d.ink, d.savingsChip)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(d.link, d.coursesChip)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(d.okInk, d.savingsChip)).isAtLeast(7.0)
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
        assertThat(WcagContrast.ratio(A11yTokens.Night.onGold, A11yTokens.Night.accentAuto)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(n.ink, n.coursesChip)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(n.ink, n.savingsChip)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(n.link, n.coursesChip)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(n.okInk, n.savingsChip)).isAtLeast(7.0)
    }
}
