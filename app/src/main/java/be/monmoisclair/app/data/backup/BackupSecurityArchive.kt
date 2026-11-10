package be.monmoisclair.app.data.backup

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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
        outFile.parentFile?.mkdirs()
        if (outFile.isDirectory) {
            outFile.deleteRecursively()
        } else if (outFile.exists()) {
            outFile.delete()
        }
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
                    // Lecture bornée : une entrée compressée petite ne doit pas
                    // exploser en mémoire une fois décompressée (zip bomb).
                    val data = readBounded(zip, MAX_ZIP_BYTES - running)
                    running += data.size.toLong()
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

    /**
     * Accepte `receipts/<nom>.webp` ou `.pdf` sans `..`.
     * Le PDF reste dans le même dossier que les photos (charte / benchmark).
     */
    fun sanitizeReceiptPath(relative: String): String? {
        val name = relative.trimStart('/')
        if (!name.startsWith("receipts/")) return null
        val lower = name.lowercase()
        if (!lower.endsWith(".webp") && !lower.endsWith(".pdf")) return null
        val parts = name.split('/')
        if (parts.size != 2) return null
        if (parts.any { it.isEmpty() || it == "." || it == ".." }) return null
        return "receipts/${parts[1]}"
    }

    fun isPdfReceiptPath(relative: String): Boolean {
        val safe = sanitizeReceiptPath(relative) ?: return false
        return safe.endsWith(".pdf", ignoreCase = true)
    }

    fun mimeForReceiptPath(relative: String): String? {
        val safe = sanitizeReceiptPath(relative) ?: return null
        return if (safe.endsWith(".pdf", ignoreCase = true)) {
            "application/pdf"
        } else {
            "image/webp"
        }
    }

    /**
     * Lit au plus [maxBytes] octets. Dépassement → [BackupFailure.Io]
     * (évite `readBytes()` illimité sur URI / entrée ZIP).
     */
    fun readBounded(input: InputStream, maxBytes: Long): ByteArray {
        if (maxBytes <= 0L) {
            throw BackupFailure.Io(
                "Archive trop lourde une fois ouverte. Copiez le dossier Documents plutôt."
            )
        }
        val out = ByteArrayOutputStream()
        val chunk = ByteArray(8 * 1024)
        var total = 0L
        while (true) {
            val n = input.read(chunk)
            if (n < 0) break
            total += n.toLong()
            if (total > maxBytes) {
                throw BackupFailure.Io(
                    "Archive trop lourde une fois ouverte. Copiez le dossier Documents plutôt."
                )
            }
            out.write(chunk, 0, n)
        }
        return out.toByteArray()
    }
}
