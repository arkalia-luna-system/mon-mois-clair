package be.monmoisclair.app.data.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CleartextLegacyGraceTest {

    @Test
    fun graceClosed_ignoresRequestedOptIn() {
        assertThat(CleartextLegacyGrace.OPT_IN_AVAILABLE).isFalse()
        assertThat(CleartextLegacyGrace.effectiveAllow(true)).isFalse()
        assertThat(CleartextLegacyGrace.effectiveAllow(false)).isFalse()
    }
}
