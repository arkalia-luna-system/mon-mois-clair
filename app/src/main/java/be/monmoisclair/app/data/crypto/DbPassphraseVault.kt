package be.monmoisclair.app.data.crypto

import android.content.Context
import be.monmoisclair.app.data.device.CredentialStorageGate
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Passphrase SQLCipher (32 octets aléatoires) enveloppée par [KeystoreAesKey.WRAP_ALIAS].
 * Jamais loguée. Hex UTF-8 pour `PRAGMA key` / SupportFactory.
 *
 * Stockage CE (`filesDir`) : exige utilisateur déverrouillé (pas Direct Boot).
 */
@Singleton
class DbPassphraseVault @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dir: File
        get() = File(context.filesDir, DIR)

    private val wrappedFile: File
        get() = File(ensureCryptoDir(), WRAPPED_NAME)

    private val markerFile: File
        get() = File(ensureCryptoDir(), MARKER_NAME)

    /** Lecture seule — ne crée pas le dossier `crypto/` (Direct Boot / CE). */
    fun isDbEncryptedMarkerPresent(): Boolean = File(dir, MARKER_NAME).isFile

    fun markDbEncrypted() {
        requireCredentialStorage()
        markerFile.writeText("1")
        clearMigrationFailed()
    }

    /** Migration SQLCipher Failed → Room claire temporaire (P1 confiance). */
    fun isMigrationFailedPresent(): Boolean = File(dir, MIGRATION_FAILED_NAME).isFile

    fun markMigrationFailed() {
        requireCredentialStorage()
        migrationFailedFile.writeText("1")
    }

    fun clearMigrationFailed() {
        requireCredentialStorage()
        if (migrationFailedFile.isFile) migrationFailedFile.delete()
    }

    private val migrationFailedFile: File
        get() = File(ensureCryptoDir(), MIGRATION_FAILED_NAME)

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
        requireCredentialStorage()
        if (wrappedFile.isFile && wrappedFile.length() > 0L) return
        val raw = AesGcmBox.randomBytes(32)
        try {
            val key = KeystoreAesKey.getOrCreateWrapKey()
            val wrapped = AesGcmBox.encrypt(key, raw)
            val target = wrappedFile
            val tmp = File(target.parentFile, "$WRAPPED_NAME.tmp")
            tmp.writeText(Base64.getEncoder().encodeToString(wrapped))
            if (!tmp.renameTo(target)) {
                target.writeText(tmp.readText(Charsets.UTF_8))
                tmp.delete()
            }
        } finally {
            AesGcmBox.clear(raw)
        }
    }

    private fun ensureCryptoDir(): File {
        val d = dir
        if (!d.isDirectory && !d.mkdirs() && !d.isDirectory) {
            error("Impossible de créer ${d.absolutePath} (CE / Direct Boot ?)")
        }
        return d
    }

    private fun requireCredentialStorage() {
        CredentialStorageGate.requireAvailable(context)
    }

    companion object {
        const val DIR = "crypto"
        const val WRAPPED_NAME = "db_passphrase.wrapped"
        const val MARKER_NAME = "db_encrypted.flag"
        const val MIGRATION_FAILED_NAME = "db_migration_failed.flag"
        const val DB_NAME = "mon_mois_clair.db"
    }
}
