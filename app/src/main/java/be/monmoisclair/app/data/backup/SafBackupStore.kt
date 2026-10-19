package be.monmoisclair.app.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.BackupHealth
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafBackupStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appMetaDao: AppMetaDao,
    private val codec: BackupJsonCodec,
    private val rotator: BackupRotator,
    private val healthStore: BackupHealthStore
) {

    fun buildOpenDocumentTreeIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }
    }

    suspend fun hasPersistedTree(): Boolean {
        val uri = readTreeUri() ?: return false
        return runCatching {
            SafDocumentBackupTree(context, uri)
            true
        }.getOrDefault(false)
    }

    suspend fun persistTreeUri(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flags)
        appMetaDao.upsert(AppMetaEntity(AppMetaKeys.SAF_TREE_URI, uri.toString()))
        healthStore.mark(BackupHealth.OK)
    }

    suspend fun clearPersistedTree() {
        val existing = readTreeUri()
        if (existing != null) {
            runCatching {
                context.contentResolver.releasePersistableUriPermission(
                    existing,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }
        appMetaDao.upsert(AppMetaEntity(AppMetaKeys.SAF_TREE_URI, ""))
        healthStore.mark(BackupHealth.NOT_CONFIGURED)
    }

    suspend fun writeRotatedBackup(document: BackupDocument) {
        val tree = openTreeOrThrow()
        val json = codec.encodeDocument(document)
        try {
            rotator.writeRotated(tree, json)
        } catch (e: BackupFailure) {
            healthStore.markBroken()
            throw e
        } catch (e: Exception) {
            healthStore.markBroken()
            throw BackupFailure.Io("Échec writeRotatedBackup", e)
        }
    }

    suspend fun readLatestValidBackup(): BackupDocument {
        val tree = openTreeOrThrow()
        var lastError: Exception? = null
        for (name in BackupFileNames.rotatedNewestFirst) {
            if (!tree.exists(name)) continue
            try {
                val raw = tree.readUtf8(name)
                return codec.decodeAndVerify(raw)
            } catch (e: Exception) {
                lastError = e
            }
        }
        healthStore.markBroken()
        throw BackupFailure.Integrity(
            "Aucun backup valide trouvé (1..3). Dernière erreur: ${lastError?.message}"
        )
    }

    suspend fun writeBinaryRelative(relativePath: String, bytes: ByteArray) {
        val tree = openTreeOrThrow() as? SafDocumentBackupTree
            ?: throw BackupFailure.Io("Arbre SAF requis pour les photos")
        tree.writeBinaryRelative(relativePath, bytes)
    }

    suspend fun uriForRelative(relativePath: String): Uri? {
        if (!hasPersistedTree()) return null
        return runCatching {
            (openTreeOrThrow() as SafDocumentBackupTree).uriForRelative(relativePath)
        }.getOrNull()
    }

    /** Contenu brut du dernier backup valide (pour partage T8). */
    suspend fun readLatestValidBackupUtf8(): String {
        val tree = openTreeOrThrow()
        var lastError: Exception? = null
        for (name in BackupFileNames.rotatedNewestFirst) {
            if (!tree.exists(name)) continue
            try {
                val raw = tree.readUtf8(name)
                codec.decodeAndVerify(raw)
                return raw
            } catch (e: Exception) {
                lastError = e
            }
        }
        healthStore.markBroken()
        throw BackupFailure.Integrity(
            "Aucun backup valide à partager. Dernière erreur: ${lastError?.message}"
        )
    }

    private suspend fun readTreeUri(): Uri? {
        val raw = appMetaDao.get(AppMetaKeys.SAF_TREE_URI)?.value?.trim().orEmpty()
        if (raw.isEmpty()) return null
        return Uri.parse(raw)
    }

    private suspend fun openTreeOrThrow(): BackupTree {
        val uri = readTreeUri()
            ?: run {
                healthStore.mark(BackupHealth.NOT_CONFIGURED)
                throw BackupFailure.NotConfigured()
            }
        return try {
            SafDocumentBackupTree(context, uri)
        } catch (e: BackupFailure) {
            healthStore.markBroken()
            throw e
        } catch (e: Exception) {
            healthStore.markBroken()
            throw BackupFailure.Io("Ouverture arbre SAF impossible", e)
        }
    }
}
