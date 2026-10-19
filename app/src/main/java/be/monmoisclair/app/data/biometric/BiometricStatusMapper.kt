package be.monmoisclair.app.data.biometric

import androidx.biometric.BiometricManager

/** Mapping pur BiometricManager → [BiometricAvailability] (testable). */
object BiometricStatusMapper {
    fun fromCanAuthenticate(code: Int): BiometricAvailability = when (code) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Ready
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NoneEnrolled
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.HardwareUnavailable
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN,
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
            BiometricAvailability.Unsupported
        else -> BiometricAvailability.Unsupported
    }
}
