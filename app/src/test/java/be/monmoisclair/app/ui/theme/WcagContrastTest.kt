package be.monmoisclair.app.ui.theme

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Contrastes jour ([A11yTokens.Day]) et nuit ([A11yTokens.Night]).
 * Les getters Compose de [A11yTokens] ne sont pas utilisables hors @Composable.
 */
class WcagContrastTest {

    private val day = A11yTokens.Day

    @Test
    fun paperAndButtons_meetAaa() {
        assertThat(WcagContrast.ratio(day.ink, day.paperCream)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(day.ink, day.paperCard)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(day.buttonText, day.buttonFill)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(day.inkMuted, day.paperCream)).isAtLeast(7.0)
    }

    @Test
    fun statusChips_meetAaa() {
        assertThat(WcagContrast.ratio(day.ink, day.accentAuto)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(day.buttonText, day.accentDanger)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(day.buttonText, day.accentOk)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(day.accentDanger, day.paperCream)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(day.accentOk, day.accentOkSoft)).isAtLeast(7.0)
    }

    @Test
    fun negativeRemaining_meetsPremiumContrast() {
        // Montant négatif en AccentDanger (plus AccentWarn trop pâle).
        assertThat(WcagContrast.ratio(day.accentDanger, day.paperCream)).isAtLeast(5.4)
    }

    @Test
    fun accentWarnDay_meetsAaaOnCream() {
        // Brun warn lisible sur crème (≥ 7:1) — identité warn, pas danger.
        assertThat(WcagContrast.ratio(day.accentWarn, day.paperCream)).isAtLeast(7.0)
    }

    @Test
    fun nightInk_meetsAaaOnPageCardAndWarm() {
        val night = A11yTokens.Night
        val surfaces = listOf(night.paperCream, night.paperCard, night.paperWarm)
        val inks = listOf(
            night.ink,
            night.inkMuted,
            night.dangerInk,
            night.accentOkInk,
            night.link,
            night.accentWarn
        )
        for (surface in surfaces) {
            for (ink in inks) {
                assertThat(WcagContrast.ratio(ink, surface)).isAtLeast(7.0)
            }
        }
        assertThat(WcagContrast.ratio(night.buttonText, night.buttonFill)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(night.buttonText, night.accentOk)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(night.buttonText, night.accentDanger)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(night.onGold, night.accentAuto)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(night.ink, night.accentOkSoft)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(night.accentOkInk, night.accentOkSoft)).isAtLeast(7.0)
        // PageTitle nuit : muted distinct de l’encre, toujours ≥ 7:1 sur crème.
        assertThat(night.inkMuted).isNotEqualTo(night.ink)
        assertThat(WcagContrast.ratio(night.inkMuted, night.paperCream)).isAtLeast(7.0)
    }
}
