package be.monmoisclair.app.data.biometric

/** Disponibilité matérielle / réglages système pour la biométrie. */
enum class BiometricAvailability {
    Ready,
    NoneEnrolled,
    HardwareUnavailable,
    Unsupported
}

sealed class BiometricAuthResult {
    data object Success : BiometricAuthResult()
    data object Cancelled : BiometricAuthResult()
    data class Failed(val message: String) : BiometricAuthResult()
}
