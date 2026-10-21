package be.monmoisclair.app.data.biometric

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthGatewayImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BiometricAuthGateway {

    private val authenticators: Int =
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    override fun availability(): BiometricAvailability {
        val code = BiometricManager.from(context).canAuthenticate(authenticators)
        return BiometricStatusMapper.fromCanAuthenticate(code)
    }

    override fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        @Suppress("UNUSED_PARAMETER") negativeButton: String,
        onResult: (BiometricAuthResult) -> Unit
    ) {
        // DEVICE_CREDENTIAL : BiometricPrompt interdit setNegativeButtonText.
        when (availability()) {
            BiometricAvailability.Ready -> Unit
            BiometricAvailability.NoneEnrolled -> {
                onResult(
                    BiometricAuthResult.Failed(
                        "Aucune empreinte ni code d’écran configuré sur ce téléphone."
                    )
                )
                return
            }
            BiometricAvailability.HardwareUnavailable -> {
                onResult(
                    BiometricAuthResult.Failed(
                        "Empreinte ou code temporairement indisponible. Réessayez dans un instant."
                    )
                )
                return
            }
            BiometricAvailability.Unsupported -> {
                onResult(
                    BiometricAuthResult.Failed(
                        "Ce téléphone ne permet pas le verrouillage par empreinte ou code."
                    )
                )
                return
            }
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    onResult(BiometricAuthResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    ) {
                        onResult(BiometricAuthResult.Cancelled)
                    } else {
                        Log.e("BiometricAuth", "errorCode=$errorCode $errString")
                        onResult(
                            BiometricAuthResult.Failed(
                                "Déverrouillage impossible. Réessayez, ou utilisez le code du téléphone."
                            )
                        )
                    }
                }

                override fun onAuthenticationFailed() {
                    // Essai raté : le système laisse réessayer ; pas de callback final.
                }
            }
        )

        // DEVICE_CREDENTIAL : pas de negative button (API BiometricPrompt).
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
        prompt.authenticate(builder.build())
    }
}
