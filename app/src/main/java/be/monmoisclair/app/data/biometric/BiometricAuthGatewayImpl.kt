package be.monmoisclair.app.data.biometric

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import be.monmoisclair.app.data.crypto.KeystoreAesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthGatewayImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BiometricAuthGateway {

    /** UI gate (WEAK) — compatible Face Unlock « convenience ». */
    private val uiAuthenticators: Int =
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    /**
     * CryptoObject exige STRONG (ou credential appareil). API 30+.
     */
    private val cryptoAuthenticators: Int =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    override fun availability(): BiometricAvailability {
        val code = BiometricManager.from(context).canAuthenticate(uiAuthenticators)
        return BiometricStatusMapper.fromCanAuthenticate(code)
    }

    override fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        @Suppress("UNUSED_PARAMETER") negativeButton: String,
        preferCrypto: Boolean,
        onResult: (BiometricAuthResult) -> Unit
    ) {
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

        val crypto = if (preferCrypto) prepareCryptoObject() else null
        val authenticators = if (crypto != null) cryptoAuthenticators else uiAuthenticators

        // Si Crypto demandé mais STRONG indisponible → retomber sur UI gate (DB déjà SQLCipher).
        if (crypto != null) {
            val strongCode = BiometricManager.from(context).canAuthenticate(cryptoAuthenticators)
            if (strongCode != BiometricManager.BIOMETRIC_SUCCESS) {
                Log.i(TAG, "CryptoObject indisponible (code=$strongCode) — porte UI")
                authenticatePlain(activity, title, subtitle, uiAuthenticators, onResult)
                return
            }
        }

        if (crypto != null) {
            authenticateWithCrypto(activity, title, subtitle, authenticators, crypto, onResult)
        } else {
            authenticatePlain(activity, title, subtitle, authenticators, onResult)
        }
    }

    private fun prepareCryptoObject(): BiometricPrompt.CryptoObject? {
        if (Build.VERSION.SDK_INT < 28) return null
        val cipher = KeystoreAesKey.unlockCipherForAuth() ?: return null
        return runCatching { BiometricPrompt.CryptoObject(cipher) }.getOrNull()
    }

    private fun authenticateWithCrypto(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        authenticators: Int,
        crypto: BiometricPrompt.CryptoObject,
        onResult: (BiometricAuthResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    // Preuve crypto : Cipher utilisable après auth (on ne persiste pas le résultat).
                    val ok = result.cryptoObject?.cipher != null
                    if (ok) {
                        onResult(BiometricAuthResult.Success)
                    } else {
                        // Certains OEM renvoient Success sans cryptoObject — accepter + log.
                        Log.w(TAG, "Success sans CryptoObject — accepté")
                        onResult(BiometricAuthResult.Success)
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    mapError(errorCode, onResult)
                }

                override fun onAuthenticationFailed() = Unit
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info, crypto)
    }

    private fun authenticatePlain(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        authenticators: Int,
        onResult: (BiometricAuthResult) -> Unit
    ) {
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
                    mapError(errorCode, onResult)
                }

                override fun onAuthenticationFailed() = Unit
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }

    private fun mapError(errorCode: Int, onResult: (BiometricAuthResult) -> Unit) {
        if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
            errorCode == BiometricPrompt.ERROR_CANCELED
        ) {
            onResult(BiometricAuthResult.Cancelled)
        } else {
            Log.e(TAG, "errorCode=$errorCode")
            onResult(
                BiometricAuthResult.Failed(
                    "Déverrouillage impossible. Réessayez, ou utilisez le code du téléphone."
                )
            )
        }
    }

    companion object {
        private const val TAG = "BiometricAuth"
    }
}
