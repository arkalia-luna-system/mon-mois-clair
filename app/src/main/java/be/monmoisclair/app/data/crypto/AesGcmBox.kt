package be.monmoisclair.app.data.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM utilitaire (IV aléatoire préfixé au ciphertext).
 * Pas de logging du plaintext / clé.
 *
 * **Android Keystore** : les clés avec `setRandomizedEncryptionRequired(true)`
 * refusent un IV fourni par l’appelant en ENCRYPT (`Caller-provided IV not permitted`).
 * L’IV est donc toujours généré par le [Cipher] (`init` sans params), puis préfixé.
 * Le déchiffrement fournit bien l’IV (obligatoire Keystore / GCM).
 */
object AesGcmBox {

    const val IV_BYTES = 12
    const val TAG_BITS = 128
    const val KEY_BITS = 256
    const val SALT_BYTES = 16
    private const val PBKDF2_ITERATIONS = 120_000

    fun randomBytes(size: Int): ByteArray {
        val out = ByteArray(size)
        SecureRandom().nextBytes(out)
        return out
    }

    fun encrypt(key: SecretKey, plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        // Pas de GCMParameterSpec ici : compatible AndroidKeyStore (CALLER_NONCE interdit).
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
            ?: error("AES-GCM sans IV généré par le provider")
        require(iv.size == IV_BYTES) { "IV GCM inattendu: ${iv.size} octets" }
        val sealed = cipher.doFinal(plaintext)
        return iv + sealed
    }

    fun decrypt(key: SecretKey, blob: ByteArray): ByteArray {
        require(blob.size > IV_BYTES) { "blob trop court" }
        val iv = blob.copyOfRange(0, IV_BYTES)
        val sealed = blob.copyOfRange(IV_BYTES, blob.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        return cipher.doFinal(sealed)
    }

    /** Dérive une clé AES-256 depuis un code famille (PBKDF2-HMAC-SHA256). */
    fun keyFromSecret(secret: CharArray, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(secret, salt, PBKDF2_ITERATIONS, KEY_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }

    fun clear(bytes: ByteArray?) {
        bytes?.fill(0)
    }
}
