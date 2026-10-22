package be.monmoisclair.app.ui.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** V-01 : titleMedium = Source Sans ≥ 18 sp (pas fuite Roboto / serif). */
@RunWith(RobolectricTestRunner::class)
class CarnetTypographyTest {

    @Test
    fun titleMedium_isUiSansAtLeast18Sp() {
        val ink = A11yTokens.Day.ink
        val muted = A11yTokens.Day.inkMuted
        val ty = carnetTypography(ink, muted)

        assertThat(ty.titleMedium.fontSize).isEqualTo(A11yTokens.minBodySp)
        assertThat(ty.titleMedium.fontSize.value).isAtLeast(18f)
        assertThat(ty.titleMedium.fontWeight).isEqualTo(FontWeight.SemiBold)
        // Même famille que le corps (UiSans), pas le serif du Reste.
        assertThat(ty.titleMedium.fontFamily).isEqualTo(ty.bodyLarge.fontFamily)
        assertThat(ty.titleMedium.fontFamily).isNotEqualTo(ty.displayLarge.fontFamily)
        // Slots voisins aussi branchés (évite Roboto Material par défaut).
        assertThat(ty.titleSmall.fontSize).isEqualTo(18.sp)
        assertThat(ty.labelMedium.fontFamily).isEqualTo(ty.bodyLarge.fontFamily)
    }
}
