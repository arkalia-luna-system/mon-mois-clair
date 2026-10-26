package be.monmoisclair.app.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import be.monmoisclair.app.data.crypto.BackupEnvelope
import be.monmoisclair.app.data.crypto.LocalFileCipher
import be.monmoisclair.app.data.crypto.ShareSecretVault
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
    private val healthStore: BackupHealthStore,
    private val shareSecretVault: ShareSecretVault
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
        val secret = shareSecretVault.getOrCreateSecretChars()
        try {
            val sealed = BackupEnvelope.sealToUtf8(json, secret)
            rotator.writeRotated(tree, sealed)
        } catch (e: BackupFailure) {
            healthStore.markBroken()
            throw e
        } catch (e: Exception) {
            healthStore.markBroken()
            throw BackupFailure.Io("Impossible d’écrire la sauvegarde dans Documents.", e)
        } finally {
            secret.fill('\u0000')
        }
    }

    suspend fun readLatestValidBackup(): BackupDocument {
        val tree = openTreeOrThrow()
        val secret = shareSecretVault.getOrCreateSecretChars()
        try {
            for (name in BackupFileNames.rotatedNewestFirst) {
                if (!tree.exists(name)) continue
                try {
                    val raw = tree.readUtf8(name)
                    val json = decodeMaybeSealedUtf8(raw, secret)
                    return codec.decodeAndVerify(json)
                } catch (_: Exception) {
                    // Essayer le fichier de rotation suivant.
                }
            }
        } finally {
            secret.fill('\u0000')
        }
        healthStore.markBroken()
        throw BackupFailure.Integrity(
            "Aucune sauvegarde valide dans Documents."
        )
    }

    suspend fun writeBinaryRelative(relativePath: String, bytes: ByteArray) {
        val tree = openTreeOrThrow() as? SafDocumentBackupTree
            ?: throw BackupFailure.Io("Dossier Documents requis pour les photos.")
        // Documents SAF : même enveloppe locale (MRF1) pour ne pas laisser la photo en clair hors app.
        tree.writeBinaryRelative(relativePath, LocalFileCipher.seal(bytes))
    }

    suspend fun uriForRelative(relativePath: String): Uri? {
        if (!hasPersistedTree()) return null
        return runCatching {
            (openTreeOrThrow() as SafDocumentBackupTree).uriForRelative(relativePath)
        }.getOrNull()
    }

    /** Photos présentes dans le dossier SAF (pour partage) — déchiffrées si MRF1. */
    suspend fun listReceiptBinaries(): Map<String, ByteArray> {
        val tree = openTreeOrThrow() as? SafDocumentBackupTree
            ?: return emptyMap()
        return tree.listReceiptBinaries().mapValues { (_, bytes) ->
            LocalFileCipher.open(bytes)
        }
    }

    /**
     * JSON **en clair** du dernier backup valide (pour construire le ZIP/MMC de partage).
     * Les fichiers SAF peuvent être chiffrés (MMC1B64) — on déchiffre avec le code local.
     */
    suspend fun readLatestValidBackupUtf8(): String {
        val tree = openTreeOrThrow()
        val secret = shareSecretVault.getOrCreateSecretChars()
        try {
            for (name in BackupFileNames.rotatedNewestFirst) {
                if (!tree.exists(name)) continue
                try {
                    val raw = tree.readUtf8(name)
                    val json = decodeMaybeSealedUtf8(raw, secret)
                    codec.decodeAndVerify(json)
                    return json
                } catch (_: Exception) {
                    // Essayer le fichier de rotation suivant.
                }
            }
        } finally {
            secret.fill('\u0000')
        }
        healthStore.markBroken()
        throw BackupFailure.Integrity(
            "Aucune sauvegarde valide à partager."
        )
    }

    /** Déchiffre MMC1B64 si besoin ; sinon grace copies claires pré-6.81. */
    private fun decodeMaybeSealedUtf8(raw: String, secret: CharArray): String {
        if (BackupEnvelope.looksLikeText(raw)) {
            return BackupEnvelope.openUtf8(raw, secret)
        }
        return raw
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
            throw BackupFailure.Io("Dossier Documents inaccessible.", e)
        }
    }
}
