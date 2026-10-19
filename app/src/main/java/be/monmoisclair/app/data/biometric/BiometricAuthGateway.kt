package be.monmoisclair.app.data.biometric

import androidx.fragment.app.FragmentActivity

/**
 * Abstraction autour de BiometricPrompt / BiometricManager
 * (testable + pas de dépendance Compose dans la couche data).
 */
interface BiometricAuthGateway {
    fun availability(): BiometricAvailability

    /**
     * Affiche le prompt système. [onResult] est appelé sur le thread principal.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButton: String,
        onResult: (BiometricAuthResult) -> Unit
    )
}
