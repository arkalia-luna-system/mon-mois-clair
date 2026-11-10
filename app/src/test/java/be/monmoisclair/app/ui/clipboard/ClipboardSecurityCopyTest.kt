package be.monmoisclair.app.ui.clipboard

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ClipboardSecurityCopyTest {

    @Test
    fun clearSeconds_matchesSensitiveClipboard() {
        assertThat(ClipboardSecurityCopy.CLEAR_SECONDS * 1_000L)
            .isEqualTo(SensitiveClipboard.CLEAR_AFTER_MS)
    }

    @Test
    fun howItWorks_mentionsIbanAndTtl() {
        val body = ClipboardSecurityCopy.HOW_IT_WORKS_BODY
        assertThat(body).contains("IBAN")
        assertThat(body).contains("communication")
        assertThat(body).contains("30")
        assertThat(body).contains("quittez")
    }
}
