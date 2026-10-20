package be.monmoisclair.app.ui.theme

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WcagContrastTest {

    @Test
    fun paperAndButtons_meetAaa() {
        assertThat(WcagContrast.ratio(A11yTokens.Ink, A11yTokens.PaperCream)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(A11yTokens.Ink, A11yTokens.PaperCard)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(A11yTokens.ButtonText, A11yTokens.ButtonFill)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(A11yTokens.InkMuted, A11yTokens.PaperCream)).isAtLeast(7.0)
    }

    @Test
    fun statusChips_meetAaa() {
        assertThat(WcagContrast.ratio(A11yTokens.Ink, A11yTokens.AccentAuto)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(A11yTokens.ButtonText, A11yTokens.AccentDanger)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(A11yTokens.ButtonText, A11yTokens.AccentOk)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(A11yTokens.AccentDanger, A11yTokens.PaperCream)).isAtLeast(7.0)
        assertThat(WcagContrast.ratio(A11yTokens.AccentOk, A11yTokens.AccentOkSoft)).isAtLeast(7.0)
    }

    @Test
    fun negativeRemaining_meetsPremiumContrast() {
        // Montant négatif en AccentDanger (plus AccentWarn trop pâle).
        assertThat(WcagContrast.ratio(A11yTokens.AccentDanger, A11yTokens.PaperCream)).isAtLeast(5.4)
    }
}
