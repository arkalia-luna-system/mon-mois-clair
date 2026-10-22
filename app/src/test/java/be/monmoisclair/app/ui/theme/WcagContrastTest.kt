package be.monmoisclair.app.ui.theme

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Contrastes sur la palette **jour** ([A11yTokens.Day]).
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
    fun night_dangerAndLink_readableOnInk() {
        val night = A11yTokens.Night
        assertThat(WcagContrast.ratio(night.dangerInk, night.paperCream)).isAtLeast(4.5)
        assertThat(WcagContrast.ratio(night.link, night.paperCream)).isAtLeast(4.5)
        assertThat(WcagContrast.ratio(night.buttonText, night.buttonFill)).isAtLeast(7.0)
    }
}
