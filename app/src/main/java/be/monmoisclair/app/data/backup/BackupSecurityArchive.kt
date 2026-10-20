package be.monmoisclair.app.data.backup

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Archive de sécurité : JSON des mois + photos webp du dossier receipts.
 * Entrée JSON : [BackupFileNames.V1] (ou premier `*.json` à la restauration).
 */
object BackupSecurityArchive {

    const val SHARE_ZIP_NAME = "mon-mois-clair-securite.zip"
    /** Au-delà : mieux copier le dossier Documents à la main. */
    const val MAX_ZIP_BYTES = 45L * 1024L * 1024L

    data class Packed(
        val file: File,
        val photoCount: Int,
        val totalBytes: Long
    )

    data class Unpacked(
        val jsonUtf8: String,
        val receipts: Map<String, ByteArray>
    )

    fun packToFile(
        outFile: File,
        jsonUtf8: String,
        receipts: Map<String, ByteArray>
    ): Packed {
        if (outFile.exists()) outFile.delete()
        var photoCount = 0
        var running = 0L
        val jsonBytes = jsonUtf8.toByteArray(Charsets.UTF_8)
        running += jsonBytes.size.toLong()
        if (running > MAX_ZIP_BYTES) {
            throw BackupFailure.Io(
                "Archive trop lourde. Copiez le dossier Documents plutôt que d’envoyer le message."
            )
        }
        ZipOutputStream(FileOutputStream(outFile)).use { zip ->
            zip.putNextEntry(ZipEntry(BackupFileNames.V1))
            zip.write(jsonBytes)
            zip.closeEntry()
            for ((relative, bytes) in receipts) {
                val name = sanitizeReceiptPath(relative) ?: continue
                running += bytes.size.toLong()
                if (running > MAX_ZIP_BYTES) {
                    outFile.delete()
                    throw BackupFailure.Io(
                        "Archive trop lourde (${running / (1024 * 1024)} Mo). " +
                            "Copiez le dossier Documents plutôt que d’envoyer le message."
                    )
                }
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
                photoCount++
            }
        }
        val size = outFile.length()
        if (size > MAX_ZIP_BYTES) {
            outFile.delete()
            throw BackupFailure.Io(
                "Archive trop lourde (${size / (1024 * 1024)} Mo). " +
                    "Copiez le dossier Documents plutôt que d’envoyer le message."
            )
        }
        return Packed(file = outFile, photoCount = photoCount, totalBytes = size)
    }

    fun unpack(bytes: ByteArray): Unpacked {
        if (bytes.size > MAX_ZIP_BYTES) {
            throw BackupFailure.Io(
                "Archive trop lourde. Copiez le dossier Documents plutôt."
            )
        }
        var json: String? = null
        val receipts = linkedMapOf<String, ByteArray>()
        var running = 0L
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val name = entry.name.trimStart('/')
                    val data = zip.readBytes()
                    running += data.size.toLong()
                    if (running > MAX_ZIP_BYTES) {
                        throw BackupFailure.Io(
                            "Archive trop lourde une fois ouverte. " +
                                "Copiez le dossier Documents plutôt."
                        )
                    }
                    when {
                        name == BackupFileNames.V1 ||
                            (name.endsWith(".json") && !name.contains('/')) -> {
                            if (json == null) json = data.toString(Charsets.UTF_8)
                        }
                        else -> {
                            val safe = sanitizeReceiptPath(name)
                            if (safe != null) receipts[safe] = data
                        }
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        val body = json ?: throw BackupFailure.Io("Archive sans fichier de sauvegarde.")
        return Unpacked(jsonUtf8 = body, receipts = receipts)
    }

    fun looksLikeZip(bytes: ByteArray): Boolean =
        bytes.size >= 4 &&
            bytes[0] == 0x50.toByte() &&
            bytes[1] == 0x4B.toByte() &&
            bytes[2] == 0x03.toByte() &&
            bytes[3] == 0x04.toByte()

    /** Accepte seulement `receipts/<nom>.webp` sans `..`. */
    fun sanitizeReceiptPath(relative: String): String? {
        val name = relative.trimStart('/')
        if (!name.startsWith("receipts/")) return null
        if (!name.endsWith(".webp", ignoreCase = true)) return null
        val parts = name.split('/')
        if (parts.size != 2) return null
        if (parts.any { it.isEmpty() || it == "." || it == ".." }) return null
        return "receipts/${parts[1]}"
    }
}
