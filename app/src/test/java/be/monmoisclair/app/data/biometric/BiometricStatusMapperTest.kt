package be.monmoisclair.app.data.biometric

import androidx.biometric.BiometricManager
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BiometricStatusMapperTest {

    @Test
    fun success_is_ready() {
        assertThat(BiometricStatusMapper.fromCanAuthenticate(BiometricManager.BIOMETRIC_SUCCESS))
            .isEqualTo(BiometricAvailability.Ready)
    }

    @Test
    fun none_enrolled() {
        assertThat(
            BiometricStatusMapper.fromCanAuthenticate(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
        ).isEqualTo(BiometricAvailability.NoneEnrolled)
    }

    @Test
    fun hw_unavailable() {
        assertThat(
            BiometricStatusMapper.fromCanAuthenticate(BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)
        ).isEqualTo(BiometricAvailability.HardwareUnavailable)
        assertThat(
            BiometricStatusMapper.fromCanAuthenticate(BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE)
        ).isEqualTo(BiometricAvailability.HardwareUnavailable)
    }

    @Test
    fun unsupported_codes() {
        assertThat(
            BiometricStatusMapper.fromCanAuthenticate(BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED)
        ).isEqualTo(BiometricAvailability.Unsupported)
        assertThat(BiometricStatusMapper.fromCanAuthenticate(-999))
            .isEqualTo(BiometricAvailability.Unsupported)
    }
}
