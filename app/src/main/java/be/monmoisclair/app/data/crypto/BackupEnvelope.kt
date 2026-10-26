package be.monmoisclair.app.data.crypto

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Enveloppe versionnée pour copies SAF / ZIP partagées.
 *
 * Binaire : `MMC1` + ver(1) + salt(16) + iv+ciphertext AES-GCM (clé = PBKDF2 du code).
 * Texte SAF : préfixe [TEXT_PREFIX] + Base64(NO_WRAP) du binaire (compatible BackupTree UTF-8).
 */
object BackupEnvelope {

    val MAGIC: ByteArray = byteArrayOf('M'.code.toByte(), 'M'.code.toByte(), 'C'.code.toByte(), '1'.code.toByte())
    const val VERSION: Byte = 1
    const val TEXT_PREFIX = "MMC1B64:"
    const val SHARE_MMC_NAME = "mon-mois-clair-copie.mmc"

    /** Ancien nom ZIP (grace restore). */
    const val LEGACY_ZIP_NAME = "mon-mois-clair-securite.zip"

    fun looksLikeBinary(bytes: ByteArray): Boolean =
        bytes.size >= 4 + 1 + AesGcmBox.SALT_BYTES + AesGcmBox.IV_BYTES + 16 &&
            bytes[0] == MAGIC[0] &&
            bytes[1] == MAGIC[1] &&
            bytes[2] == MAGIC[2] &&
            bytes[3] == MAGIC[3]

    fun looksLikeText(utf8: String): Boolean = utf8.startsWith(TEXT_PREFIX)

    fun seal(plaintext: ByteArray, secret: CharArray): ByteArray {
        val salt = AesGcmBox.randomBytes(AesGcmBox.SALT_BYTES)
        val key = AesGcmBox.keyFromSecret(secret, salt)
        return try {
            val body = AesGcmBox.encrypt(key, plaintext)
            ByteBuffer.allocate(4 + 1 + salt.size + body.size)
                .put(MAGIC)
                .put(VERSION)
                .put(salt)
                .put(body)
                .array()
        } finally {
            // SecretKeySpec n’expose pas destroy utilement ; salt déjà dans blob.
        }
    }

    fun sealToUtf8(plaintextUtf8: String, secret: CharArray): String {
        val sealed = seal(plaintextUtf8.toByteArray(StandardCharsets.UTF_8), secret)
        return TEXT_PREFIX + Base64.getEncoder().encodeToString(sealed)
    }

    fun open(sealed: ByteArray, secret: CharArray): ByteArray {
        require(looksLikeBinary(sealed)) { "pas une copie chiffrée" }
        val ver = sealed[4]
        require(ver == VERSION) { "version enveloppe $ver" }
        val saltStart = 5
        val saltEnd = saltStart + AesGcmBox.SALT_BYTES
        val salt = sealed.copyOfRange(saltStart, saltEnd)
        val body = sealed.copyOfRange(saltEnd, sealed.size)
        val key = AesGcmBox.keyFromSecret(secret, salt)
        return AesGcmBox.decrypt(key, body)
    }

    fun openUtf8(sealedUtf8: String, secret: CharArray): String {
        require(looksLikeText(sealedUtf8)) { "pas une copie chiffrée" }
        val b64 = sealedUtf8.removePrefix(TEXT_PREFIX)
        val sealed = Base64.getDecoder().decode(b64)
        return open(sealed, secret).toString(StandardCharsets.UTF_8)
    }

    /**
     * Ouvre un blob reçu (fichier / URI) : enveloppe MMC, sinon null (clair à traiter ailleurs).
     */
    fun tryOpen(bytes: ByteArray, secret: CharArray): ByteArray? {
        if (looksLikeBinary(bytes)) return open(bytes, secret)
        val asText = runCatching { bytes.toString(StandardCharsets.UTF_8) }.getOrNull() ?: return null
        if (looksLikeText(asText)) {
            return openUtf8(asText, secret).toByteArray(StandardCharsets.UTF_8)
        }
        return null
    }
}
