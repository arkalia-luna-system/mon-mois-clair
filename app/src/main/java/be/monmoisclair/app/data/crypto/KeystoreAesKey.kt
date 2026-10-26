package be.monmoisclair.app.data.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Clés AES dans Android Keystore (hardware-backed si dispo).
 *
 * - [wrapAlias] : enveloppe la passphrase SQLCipher / secrets locaux (sans auth utilisateur
 *   à chaque unwrap — le process peut ouvrir Room pour widget / rappels).
 * - [unlockAlias] : clé **liée** empreinte / code écran ; utilisée via [Cipher] +
 *   [androidx.biometric.BiometricPrompt.CryptoObject] pour le déverrouillage UI.
 *
 * Fallback logiciel uniquement si AndroidKeyStore absent (Robolectric / JVM tests).
 */
object KeystoreAesKey {

    private const val TAG = "KeystoreAesKey"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    const val WRAP_ALIAS = "mmc_db_wrap_v1"
    const val UNLOCK_ALIAS = "mmc_unlock_v1"

    @Volatile
    private var softwareWrapKey: SecretKey? = null

    fun getOrCreateWrapKey(): SecretKey = try {
        getOrCreate(
            alias = WRAP_ALIAS,
            userAuthenticationRequired = false
        )
    } catch (e: Exception) {
        Log.w(TAG, "AndroidKeyStore indisponible — clé wrap logicielle (tests)", e)
        softwareWrapKey ?: synchronized(this) {
            softwareWrapKey ?: SecretKeySpec(
                ByteArray(32).also { SecureRandom().nextBytes(it) },
                "AES"
            ).also { softwareWrapKey = it }
        }
    }

    /**
     * Clé d’unlock CryptoObject. BIOMETRIC_STRONG | DEVICE_CREDENTIAL (API 30+).
     * Si la création échoue (appareil sans strong), retourne null → fallback porte UI.
     */
    fun getOrCreateUnlockKey(): SecretKey? = runCatching {
        getOrCreate(
            alias = UNLOCK_ALIAS,
            userAuthenticationRequired = true
        )
    }.getOrNull()

    fun deleteUnlockKey() {
        runCatching {
            val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (ks.containsAlias(UNLOCK_ALIAS)) ks.deleteEntry(UNLOCK_ALIAS)
        }
    }

    fun unlockCipherForAuth(): Cipher? {
        val key = getOrCreateUnlockKey() ?: return null
        return runCatching {
            Cipher.getInstance("AES/GCM/NoPadding").also {
                // ENCRYPT : IV généré ; on jette le résultat — seul le succès d’auth compte.
                it.init(Cipher.ENCRYPT_MODE, key)
            }
        }.getOrNull()
    }

    private fun getOrCreate(alias: String, userAuthenticationRequired: Boolean): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = ks.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey

        val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        val builder = KeyGenParameterSpec.Builder(alias, purposes)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(AesGcmBox.KEY_BITS)
            .setRandomizedEncryptionRequired(true)

        if (userAuthenticationRequired) {
            builder.setUserAuthenticationRequired(true)
            if (Build.VERSION.SDK_INT >= 30) {
                builder.setUserAuthenticationParameters(
                    0, // chaque usage
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
                )
            } else {
                @Suppress("DEPRECATION")
                builder.setUserAuthenticationValidityDurationSeconds(-1)
            }
        }

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGen.init(builder.build())
        return keyGen.generateKey()
    }
}
