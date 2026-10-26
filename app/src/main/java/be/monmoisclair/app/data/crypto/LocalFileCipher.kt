package be.monmoisclair.app.data.crypto

import java.io.File
import java.nio.ByteBuffer

/**
 * Fichiers pièces jointes au repos : magic `MRF1` + AES-GCM (clé Keystore wrap).
 * Les fichiers sans magic sont traités comme **clair** (migration / grace).
 */
object LocalFileCipher {

    val MAGIC: ByteArray = byteArrayOf('M'.code.toByte(), 'R'.code.toByte(), 'F'.code.toByte(), '1'.code.toByte())

    fun looksEncrypted(bytes: ByteArray): Boolean =
        bytes.size >= 4 + AesGcmBox.IV_BYTES + 16 &&
            bytes[0] == MAGIC[0] &&
            bytes[1] == MAGIC[1] &&
            bytes[2] == MAGIC[2] &&
            bytes[3] == MAGIC[3]

    fun seal(plaintext: ByteArray): ByteArray {
        val key = KeystoreAesKey.getOrCreateWrapKey()
        val body = AesGcmBox.encrypt(key, plaintext)
        return ByteBuffer.allocate(4 + body.size).put(MAGIC).put(body).array()
    }

    fun open(blob: ByteArray): ByteArray {
        if (!looksEncrypted(blob)) return blob
        val key = KeystoreAesKey.getOrCreateWrapKey()
        return AesGcmBox.decrypt(key, blob.copyOfRange(4, blob.size))
    }

    fun writeEncrypted(file: File, plaintext: ByteArray) {
        file.parentFile?.mkdirs()
        file.writeBytes(seal(plaintext))
    }

    fun readDecrypted(file: File): ByteArray? {
        if (!file.isFile) return null
        return open(file.readBytes())
    }

    /** Migre un dossier `receipts/` : clair → MRF1. Ignore déjà chiffrés. */
    fun migrateDirectory(receiptsDir: File): Int {
        if (!receiptsDir.isDirectory) return 0
        var count = 0
        receiptsDir.listFiles()?.forEach { f ->
            if (!f.isFile) return@forEach
            val name = f.name.lowercase()
            if (!name.endsWith(".webp") && !name.endsWith(".pdf")) return@forEach
            val raw = f.readBytes()
            if (looksEncrypted(raw)) return@forEach
            writeEncrypted(f, raw)
            AesGcmBox.clear(raw)
            count++
        }
        return count
    }
}
