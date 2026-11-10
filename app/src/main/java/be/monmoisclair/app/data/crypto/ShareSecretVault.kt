package be.monmoisclair.app.data.crypto

import android.content.Context
import be.monmoisclair.app.data.device.CredentialStorageGate
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * « Code de copie » famille (8 chiffres) — une seule notion pour chiffrer SAF + ZIP WhatsApp.
 * Enveloppé Keystore sur l’appareil ; à saisir sur un **autre** téléphone pour restaurer.
 */
@Singleton
class ShareSecretVault @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dir: File
        get() = File(context.filesDir, DbPassphraseVault.DIR)

    private val file: File
        get() = File(ensureCryptoDir(), FILE_NAME)

    /** Garantit un code ; retourne les chars (à zero-fill après usage si besoin). */
    fun getOrCreateSecretChars(): CharArray {
        ensureProvisioned()
        return unwrap().toCharArray()
    }

    fun peekDisplayCode(): String {
        ensureProvisioned()
        return unwrap()
    }

    fun ensureProvisioned() {
        requireCredentialStorage()
        if (file.isFile && file.length() > 0L) return
        val code = generateNumericCode(8)
        val key = KeystoreAesKey.getOrCreateWrapKey()
        val wrapped = AesGcmBox.encrypt(key, code.toByteArray(Charsets.UTF_8))
        file.writeText(Base64.getEncoder().encodeToString(wrapped))
    }

    private fun unwrap(): String {
        requireCredentialStorage()
        val b64 = file.readText(Charsets.UTF_8).trim()
        val wrapped = Base64.getDecoder().decode(b64)
        val key = KeystoreAesKey.getOrCreateWrapKey()
        val plain = AesGcmBox.decrypt(key, wrapped)
        return plain.toString(Charsets.UTF_8).also { AesGcmBox.clear(plain) }
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
        const val FILE_NAME = "share_secret.wrapped"

        fun generateNumericCode(digits: Int): String {
            require(digits in 4..12)
            val bytes = AesGcmBox.randomBytes(digits)
            val sb = StringBuilder(digits)
            for (i in 0 until digits) {
                sb.append((bytes[i].toInt() and 0xFF) % 10)
            }
            AesGcmBox.clear(bytes)
            return sb.toString()
        }
    }
}
