package be.monmoisclair.app.data.biometric

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BiometricMessagesTest {

    @Test
    fun messages_stayGenericAcrossDevices() {
        assertThat(BiometricMessages.NONE_ENROLLED).contains("réglages")
        assertThat(BiometricMessages.NONE_ENROLLED.lowercase()).doesNotContain("téléphone")
        assertThat(BiometricMessages.UNSUPPORTED.lowercase()).contains("appareil")
        assertThat(BiometricMessages.UNSUPPORTED.lowercase()).doesNotContain("téléphone")
    }
}
