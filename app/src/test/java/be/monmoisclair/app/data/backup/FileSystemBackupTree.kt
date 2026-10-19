package be.monmoisclair.app.data.backup

import java.io.File
import java.io.FileOutputStream

/**
 * Implémentation fichier local **pour tests JVM** uniquement.
 * Garantit flush + FileDescriptor.sync() avant fermeture.
 */
class FileSystemBackupTree(
    private val directory: File
) : BackupTree {

    init {
        if (!directory.exists() && !directory.mkdirs()) {
            throw BackupFailure.Io("Impossible de créer le dossier backup: ${directory.absolutePath}")
        }
    }

    override fun exists(fileName: String): Boolean = file(fileName).exists()

    override fun delete(fileName: String): Boolean {
        val target = file(fileName)
        if (!target.exists()) return true
        return target.delete()
    }

    override fun readUtf8(fileName: String): String {
        val target = file(fileName)
        if (!target.exists()) {
            throw BackupFailure.Io("Fichier absent: $fileName")
        }
        return target.readText(Charsets.UTF_8)
    }

    override fun writeUtf8Sync(fileName: String, content: String) {
        val target = file(fileName)
        try {
            FileOutputStream(target, false).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
                fos.flush()
                fos.fd.sync()
            }
        } catch (e: Exception) {
            throw BackupFailure.Io("Écriture sync échouée: $fileName", e)
        }
    }

    override fun rename(fromName: String, toName: String) {
        val from = file(fromName)
        val to = file(toName)
        if (!from.exists()) {
            throw BackupFailure.Io("Rename: source absente $fromName")
        }
        if (to.exists() && !to.delete()) {
            throw BackupFailure.Io("Rename: impossible d'effacer la cible $toName")
        }
        if (!from.renameTo(to)) {
            try {
                to.writeText(from.readText(Charsets.UTF_8), Charsets.UTF_8)
                FileOutputStream(to, true).use { it.fd.sync() }
                if (!from.delete()) {
                    throw BackupFailure.Io("Rename fallback: source non supprimée $fromName")
                }
            } catch (e: BackupFailure) {
                throw e
            } catch (e: Exception) {
                throw BackupFailure.Io("Rename échoué: $fromName → $toName", e)
            }
        }
    }

    private fun file(name: String): File = File(directory, name)
}
