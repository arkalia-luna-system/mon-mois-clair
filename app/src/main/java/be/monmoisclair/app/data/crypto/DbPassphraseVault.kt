package be.monmoisclair.app.data.crypto

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Passphrase SQLCipher (32 octets aléatoires) enveloppée par [KeystoreAesKey.WRAP_ALIAS].
 * Jamais loguée. Hex UTF-8 pour `PRAGMA key` / SupportFactory.
 */
@Singleton
class DbPassphraseVault @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dir: File
        get() = File(context.filesDir, DIR).also { it.mkdirs() }

    private val wrappedFile: File
        get() = File(dir, WRAPPED_NAME)

    private val markerFile: File
        get() = File(dir, MARKER_NAME)

    fun isDbEncryptedMarkerPresent(): Boolean = markerFile.isFile

    fun markDbEncrypted() {
        markerFile.writeText("1")
    }

    /** Crée la passphrase si absente ; retourne les octets (à [AesGcmBox.clear] après usage). */
    fun unwrapPassphraseBytes(): ByteArray {
        ensureProvisioned()
        val b64 = wrappedFile.readText(Charsets.UTF_8).trim()
        val wrapped = Base64.getDecoder().decode(b64)
        val key = KeystoreAesKey.getOrCreateWrapKey()
        return AesGcmBox.decrypt(key, wrapped)
    }

    /** Passphrase hex (64 chars) pour SQLCipher SupportFactory. */
    fun unwrapPassphraseChars(): CharArray {
        val bytes = unwrapPassphraseBytes()
        return try {
            bytes.joinToString("") { b -> "%02x".format(b) }.toCharArray()
        } finally {
            AesGcmBox.clear(bytes)
        }
    }

    fun ensureProvisioned() {
        if (wrappedFile.isFile && wrappedFile.length() > 0L) return
        val raw = AesGcmBox.randomBytes(32)
        try {
            val key = KeystoreAesKey.getOrCreateWrapKey()
            val wrapped = AesGcmBox.encrypt(key, raw)
            wrappedFile.writeText(Base64.getEncoder().encodeToString(wrapped))
        } finally {
            AesGcmBox.clear(raw)
        }
    }

    companion object {
        const val DIR = "crypto"
        const val WRAPPED_NAME = "db_passphrase.wrapped"
        const val MARKER_NAME = "db_encrypted.flag"
        const val DB_NAME = "mon_mois_clair.db"
    }
}
