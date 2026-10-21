package be.monmoisclair.app.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.FileOutputStream

/**
 * Arbre SAF (Documents) — delete explicite avant rename, sync via ParcelFileDescriptor.
 */
class SafDocumentBackupTree(
    private val context: Context,
    treeUri: Uri
) : BackupTree {

    private val root: DocumentFile = DocumentFile.fromTreeUri(context, treeUri)
        ?: throw BackupFailure.Io("Dossier Documents inaccessible.")

    override fun exists(fileName: String): Boolean = root.findFile(fileName) != null

    override fun delete(fileName: String): Boolean {
        val existing = root.findFile(fileName) ?: return true
        return existing.delete()
    }

    override fun readUtf8(fileName: String): String {
        val file = root.findFile(fileName)
            ?: throw BackupFailure.Io("Fichier de sauvegarde introuvable dans Documents.")
        val uri = file.uri
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                return BackupSecurityArchive.readBounded(
                    input,
                    BackupSecurityArchive.MAX_ZIP_BYTES
                ).toString(Charsets.UTF_8)
            } ?: throw BackupFailure.Io("Impossible de lire la sauvegarde dans Documents.")
        } catch (e: BackupFailure) {
            throw e
        } catch (e: Exception) {
            throw BackupFailure.Io("Impossible de lire la sauvegarde dans Documents.", e)
        }
    }

    override fun writeUtf8Sync(fileName: String, content: String) {
        val existing = root.findFile(fileName)
        if (existing != null && !existing.delete()) {
            throw BackupFailure.Io("Impossible d’écrire la sauvegarde dans Documents.")
        }
        // octet-stream : évite que SAF ajoute un .json indésirable au displayName.
        val created = root.createFile("application/octet-stream", fileName)
            ?: throw BackupFailure.Io("Impossible de créer le fichier de sauvegarde dans Documents.")
        try {
            val pfd = context.contentResolver.openFileDescriptor(created.uri, "w")
                ?: throw BackupFailure.Io("Impossible d’écrire la sauvegarde dans Documents.")
            pfd.use { descriptor ->
                FileOutputStream(descriptor.fileDescriptor).use { fos ->
                    fos.write(content.toByteArray(Charsets.UTF_8))
                    fos.flush()
                    fos.fd.sync()
                }
            }
        } catch (e: BackupFailure) {
            throw e
        } catch (e: Exception) {
            throw BackupFailure.Io("Impossible d’écrire la sauvegarde dans Documents.", e)
        }
    }

    override fun rename(fromName: String, toName: String) {
        val from = root.findFile(fromName)
            ?: throw BackupFailure.Io("Rotation de sauvegarde impossible dans Documents.")
        val existingTarget = root.findFile(toName)
        if (existingTarget != null && !existingTarget.delete()) {
            throw BackupFailure.Io("Rotation de sauvegarde impossible dans Documents.")
        }
        val renamed = from.renameTo(toName)
        if (!renamed) {
            // Fallback : copie sync + delete source (DocumentFile.renameTo flaky)
            copyThenDelete(from, toName)
        }
        if (root.findFile(toName) == null) {
            throw BackupFailure.Io("Rotation de sauvegarde impossible dans Documents.")
        }
    }

    fun writeBinaryRelative(relativePath: String, bytes: ByteArray) {
        val safe = BackupSecurityArchive.sanitizeReceiptPath(relativePath)
            ?: throw BackupFailure.Io("Chemin photo invalide.")
        val parts = safe.split('/')
        var dir = root
        for (i in 0 until parts.lastIndex) {
            val name = parts[i]
            val existing = dir.findFile(name)
            dir = when {
                existing != null && existing.isDirectory -> existing
                existing != null -> throw BackupFailure.Io("Impossible d’enregistrer la photo dans Documents.")
                else -> dir.createDirectory(name)
                    ?: throw BackupFailure.Io("Impossible d’enregistrer la photo dans Documents.")
            }
        }
        val fileName = parts.last()
        dir.findFile(fileName)?.delete()
        val created = dir.createFile("image/webp", fileName)
            ?: throw BackupFailure.Io("Impossible d’enregistrer la photo dans Documents.")
        val pfd = context.contentResolver.openFileDescriptor(created.uri, "w")
            ?: throw BackupFailure.Io("Impossible d’enregistrer la photo dans Documents.")
        pfd.use { descriptor ->
            FileOutputStream(descriptor.fileDescriptor).use { fos ->
                fos.write(bytes)
                fos.flush()
                fos.fd.sync()
            }
        }
    }

    fun uriForRelative(relativePath: String): Uri? {
        val safe = BackupSecurityArchive.sanitizeReceiptPath(relativePath) ?: return null
        val parts = safe.split('/')
        var current: DocumentFile = root
        for (part in parts) {
            current = current.findFile(part) ?: return null
        }
        return current.uri
    }

    /** Photos webp du dossier receipts, pour l’archive de sécurité (S04). */
    fun listReceiptBinaries(): Map<String, ByteArray> {
        val dir = root.findFile("receipts") ?: return emptyMap()
        if (!dir.isDirectory) return emptyMap()
        val out = linkedMapOf<String, ByteArray>()
        var running = 0L
        for (child in dir.listFiles()) {
            val name = child.name ?: continue
            if (!child.isFile || !name.endsWith(".webp", ignoreCase = true)) continue
            if (name.contains("..") || name.contains('/')) continue
            val remaining = BackupSecurityArchive.MAX_ZIP_BYTES - running
            val bytes = context.contentResolver.openInputStream(child.uri)?.use {
                BackupSecurityArchive.readBounded(it, remaining)
            } ?: continue
            running += bytes.size.toLong()
            out["receipts/$name"] = bytes
        }
        return out
    }

    private fun copyThenDelete(from: DocumentFile, toName: String) {
        val bytes = context.contentResolver.openInputStream(from.uri)?.use {
            BackupSecurityArchive.readBounded(it, BackupSecurityArchive.MAX_ZIP_BYTES)
        } ?: throw BackupFailure.Io("Rotation de sauvegarde impossible dans Documents.")
        val created = root.createFile("application/octet-stream", toName)
            ?: throw BackupFailure.Io("Rotation de sauvegarde impossible dans Documents.")
        val pfd = context.contentResolver.openFileDescriptor(created.uri, "w")
            ?: throw BackupFailure.Io("Rotation de sauvegarde impossible dans Documents.")
        pfd.use { descriptor ->
            FileOutputStream(descriptor.fileDescriptor).use { fos ->
                fos.write(bytes)
                fos.flush()
                fos.fd.sync()
            }
        }
        if (!from.delete()) {
            throw BackupFailure.Io("Rotation de sauvegarde impossible dans Documents.")
        }
    }
}
