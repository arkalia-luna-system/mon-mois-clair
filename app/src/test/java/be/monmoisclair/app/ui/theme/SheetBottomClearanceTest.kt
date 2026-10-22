package be.monmoisclair.app.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class SheetBottomClearanceTest {

    @Test
    fun whenComposeInsetsZero_usesMinNavPlusClearance() {
        val got = SheetBottomClearance.resolve(
            composeInset = 0.dp,
            frameworkInset = 0.dp
        )
        assertEquals(SheetBottomClearance.MinNavBar + A11yTokens.bottomSystemClearance, got)
    }

    @Test
    fun whenFrameworkReportsNav_usesLargerPlusClearance() {
        val got = SheetBottomClearance.resolve(
            composeInset = 0.dp,
            frameworkInset = 56.dp
        )
        assertEquals(56.dp + A11yTokens.bottomSystemClearance, got)
    }

    @Test
    fun whenComposeReportsNav_usesLargerPlusClearance() {
        val got = SheetBottomClearance.resolve(
            composeInset = 48.dp,
            frameworkInset = 24.dp
        )
        assertEquals(48.dp + A11yTokens.bottomSystemClearance, got)
    }
}
