package be.monmoisclair.app.data.backup

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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
        ZipOutputStream(FileOutputStream(outFile)).use { zip ->
            zip.putNextEntry(ZipEntry(BackupFileNames.V1))
            zip.write(jsonUtf8.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            for ((relative, bytes) in receipts) {
                val name = relative.trimStart('/')
                if (!name.startsWith("receipts/")) continue
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
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
        return Packed(file = outFile, photoCount = receipts.size, totalBytes = size)
    }

    fun unpack(bytes: ByteArray): Unpacked {
        var json: String? = null
        val receipts = linkedMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val name = entry.name.trimStart('/')
                    val data = zip.readBytes()
                    when {
                        name == BackupFileNames.V1 ||
                            (name.endsWith(".json") && !name.contains('/')) -> {
                            if (json == null) json = data.toString(Charsets.UTF_8)
                        }
                        name.startsWith("receipts/") && name.endsWith(".webp") -> {
                            receipts[name] = data
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
}
