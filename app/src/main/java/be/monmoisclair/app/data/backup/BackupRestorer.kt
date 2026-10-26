package be.monmoisclair.app.data.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import be.monmoisclair.app.data.crypto.BackupEnvelope
import be.monmoisclair.app.data.crypto.LocalFileCipher
import be.monmoisclair.app.data.crypto.ShareSecretVault
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.ExpenseFeeEventEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.widget.CarnetWidgetSink
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.BackupHealth
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Restauration depuis un backup JSON vérifié.
 *
 * Règles :
 * - Valider schemaVersion + SHA-256 **avant** toute mutation DB.
 * - Remplacer le contenu dans **une seule** transaction (clear + insert).
 * - Conserver l'URI SAF **locale** du téléphone (ne pas écraser avec celle du backup).
 * - En succès : health OK, reconcile rappels, enqueue un nouveau backup (état restauré).
 * - En échec de validation : DB inchangée.
 * - Copies chiffrées (MMC) : [shareCode] requis si ce n’est pas le code local du coffre.
 */
@Singleton
class BackupRestorer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val safBackupStore: SafBackupStore,
    private val codec: BackupJsonCodec,
    private val reminderScheduler: ReminderScheduler,
    private val backupScheduler: BackupScheduler,
    private val healthStore: BackupHealthStore,
    private val widgetSink: CarnetWidgetSink,
    private val shareSecretVault: ShareSecretVault
) {

    suspend fun restoreFromLatest(): RestoreResult {
        return try {
            val document = safBackupStore.readLatestValidBackup()
            applyDocument(document)
        } catch (e: BackupFailure.NotConfigured) {
            RestoreResult.NotConfigured
        } catch (e: BackupFailure) {
            mapFailure(e)
        } catch (e: Exception) {
            healthStore.markBroken()
            RestoreResult.IoError("Impossible de lire la dernière sauvegarde.")
        }
    }

    /**
     * @param shareCode code de copie saisi (autre téléphone) ; si null, tente le code local Keystore.
     */
    suspend fun restoreFromUri(fileUri: Uri, shareCode: String? = null): RestoreResult {
        return try {
            val bytes = readUriBytes(fileUri)
            val opened = openIncomingBytes(bytes, shareCode)
                ?: return RestoreResult.BadShareCode()
            val (json, receipts) = opened
            val document = codec.decodeAndVerify(json)
            val applied = applyDocument(document)
            if (applied !is RestoreResult.Success) return applied
            if (receipts.isEmpty()) return applied
            val written = restoreReceiptsCounted(receipts)
            applied.copy(
                photosExpected = receipts.size,
                photosWritten = written
            )
        } catch (e: BackupFailure) {
            mapFailure(e)
        } catch (e: Exception) {
            RestoreResult.IoError("Impossible de lire ce fichier de sauvegarde.")
        }
    }

    /**
     * Déchiffre MMC si besoin, puis ZIP ou JSON clair (grace pré-6.81).
     * @return null si enveloppe chiffrée mais code incorrect / manquant.
     */
    private fun openIncomingBytes(
        bytes: ByteArray,
        shareCode: String?
    ): Pair<String, Map<String, ByteArray>>? {
        val asUtf8Preview = runCatching {
            // Seulement pour détecter le préfixe ASCII MMC1B64 (pas pour du binaire ZIP/MMC).
            if (bytes.size >= BackupEnvelope.TEXT_PREFIX.length &&
                bytes.copyOfRange(0, BackupEnvelope.TEXT_PREFIX.length)
                    .toString(Charsets.US_ASCII) == BackupEnvelope.TEXT_PREFIX
            ) {
                bytes.toString(Charsets.UTF_8)
            } else {
                null
            }
        }.getOrNull()

        val plainBytes: ByteArray = when {
            BackupEnvelope.looksLikeBinary(bytes) -> {
                val secret = resolveShareSecret(shareCode) ?: return null
                try {
                    BackupEnvelope.open(bytes, secret)
                } catch (_: Exception) {
                    return null
                } finally {
                    secret.fill('\u0000')
                }
            }
            asUtf8Preview != null && BackupEnvelope.looksLikeText(asUtf8Preview) -> {
                val secret = resolveShareSecret(shareCode) ?: return null
                try {
                    BackupEnvelope.openUtf8(asUtf8Preview, secret).toByteArray(Charsets.UTF_8)
                } catch (_: Exception) {
                    return null
                } finally {
                    secret.fill('\u0000')
                }
            }
            else -> bytes
        }

        return if (BackupSecurityArchive.looksLikeZip(plainBytes)) {
            val unpacked = BackupSecurityArchive.unpack(plainBytes)
            unpacked.jsonUtf8 to unpacked.receipts
        } else {
            plainBytes.toString(Charsets.UTF_8) to emptyMap()
        }
    }

    private fun resolveShareSecret(shareCode: String?): CharArray? {
        val trimmed = shareCode?.trim().orEmpty()
        if (trimmed.isNotEmpty()) return trimmed.toCharArray()
        return runCatching { shareSecretVault.getOrCreateSecretChars() }.getOrNull()
    }

    /** @return nombre de photos réellement écrites (local et/ou Documents). */
    private suspend fun restoreReceiptsCounted(receipts: Map<String, ByteArray>): Int {
        // Évite d’afficher une ancienne facture locale après restore.
        File(context.filesDir, "receipts").deleteRecursively()
        val hasSaf = safBackupStore.hasPersistedTree()
        var written = 0
        for ((relative, data) in receipts) {
            val safe = BackupSecurityArchive.sanitizeReceiptPath(relative) ?: continue
            val localOk = runCatching {
                val file = File(context.filesDir, safe)
                LocalFileCipher.writeEncrypted(file, data)
            }.isSuccess
            val safOk = if (hasSaf) {
                runCatching { safBackupStore.writeBinaryRelative(safe, data) }.isSuccess
            } else {
                false
            }
            if (localOk || safOk) written++
        }
        return written
    }

    /**
     * Point d'entrée testable : document déjà décodé/vérifié.
     */
    suspend fun restoreFromDocument(document: BackupDocument): RestoreResult {
        val current = AppMetaKeys.CURRENT_SCHEMA_VERSION.toInt()
        // Accepte les sauvegardes anciennes (1..current) : le codec remplit les champs manquants.
        if (document.schemaVersion !in 1..current) {
            return RestoreResult.UnsupportedSchema(document.schemaVersion)
        }
        // Re-vérifie l'intégrité même si l'appelant affirme que c'est valide.
        val canonical = codec.encodePayloadCanonical(document.payload)
        val recomputed = BackupChecksum.sha256Hex(canonical)
        if (!BackupChecksum.matches(document.checksumSha256, canonical)) {
            return RestoreResult.InvalidChecksum(
                expected = document.checksumSha256,
                actual = recomputed
            )
        }
        return applyDocument(document)
    }

    private suspend fun applyDocument(document: BackupDocument): RestoreResult {
        val preservedSafUri = db.appMetaDao().get(AppMetaKeys.SAF_TREE_URI)?.value
            ?.takeIf { it.isNotBlank() }

        try {
            db.withTransaction {
                clearAllTables()
                insertPayload(document.payload)
                if (preservedSafUri != null) {
                    db.appMetaDao().upsert(
                        AppMetaEntity(AppMetaKeys.SAF_TREE_URI, preservedSafUri)
                    )
                }
                db.appMetaDao().upsert(
                    AppMetaEntity(
                        AppMetaKeys.SCHEMA_VERSION,
                        AppMetaKeys.CURRENT_SCHEMA_VERSION
                    )
                )
                db.appMetaDao().upsert(
                    AppMetaEntity(AppMetaKeys.BACKUP_HEALTH, BackupHealth.OK.name)
                )
                db.appMetaDao().upsert(
                    AppMetaEntity(
                        AppMetaKeys.LAST_BACKUP_CHECKSUM_SHA256,
                        document.checksumSha256
                    )
                )
                db.appMetaDao().upsert(
                    AppMetaEntity(
                        AppMetaKeys.LAST_BACKUP_AT_EPOCH_MS,
                        document.exportedAtEpochMs.toString()
                    )
                )
            }
        } catch (e: Exception) {
            healthStore.markBroken()
            Log.e("BackupRestorer", "Transaction restore", e)
            return RestoreResult.IoError(
                "Impossible d’enregistrer la restauration. Le mois en cours n’a pas été modifié."
            )
        }

        reminderScheduler.reconcileAll()
        backupScheduler.enqueueImmediate()
        try {
            widgetSink.refresh()
        } catch (e: Exception) {
            Log.e("BackupRestorer", "widget non rafraîchi", e)
        }
        return RestoreResult.Success(document.checksumSha256)
    }

    private suspend fun clearAllTables() {
        // Enfants d'abord (FK), puis parents, puis meta.
        db.expenseFeeEventDao().clearAll()
        db.instanceDao().clearAllExpenses()
        db.instanceDao().clearAllIncomes()
        db.monthLedgerDao().clearAll()
        db.templateDao().clearAllExpenses()
        db.templateDao().clearAllIncomes()
        db.pendingLetterDao().clearAll()
        db.appMetaDao().clearAll()
    }

    private suspend fun insertPayload(payload: BackupPayload) {
        if (payload.ledgers.isNotEmpty()) {
            db.monthLedgerDao().insertAll(payload.ledgers)
        }
        if (payload.incomeTemplates.isNotEmpty()) {
            db.templateDao().insertIncomes(payload.incomeTemplates)
        }
        if (payload.expenseTemplates.isNotEmpty()) {
            db.templateDao().insertExpenses(payload.expenseTemplates)
        }
        if (payload.incomeInstances.isNotEmpty()) {
            db.instanceDao().insertIncomes(payload.incomeInstances)
        }
        if (payload.expenseInstances.isNotEmpty()) {
            db.instanceDao().insertExpenses(payload.expenseInstances)
        }
        if (payload.pendingLetters.isNotEmpty()) {
            db.pendingLetterDao().upsertAll(payload.pendingLetters)
        }
        val feeEvents = if (payload.feeEvents.isNotEmpty()) {
            payload.feeEvents
        } else {
            // Sauvegardes ≤7 : un événement synthétique par cumul lot C.
            payload.expenseTemplates.filter { it.addedFeesCents > 0L }.map { tpl ->
                ExpenseFeeEventEntity(
                    id = "fee-restored-${tpl.id}",
                    templateId = tpl.id,
                    amountCents = tpl.addedFeesCents,
                    note = tpl.addedFeesNote,
                    createdAtEpochMs = tpl.createdAtEpochMs
                )
            }
        }
        if (feeEvents.isNotEmpty()) {
            db.expenseFeeEventDao().upsertAll(feeEvents)
        }
        if (payload.appMeta.isNotEmpty()) {
            db.appMetaDao().upsertAll(payload.appMeta)
        }
    }

    private fun readUriBytes(uri: Uri): ByteArray {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val size = pfd.statSize
                if (size > 0L && size > BackupSecurityArchive.MAX_ZIP_BYTES) {
                    throw BackupFailure.Io(
                        "Fichier trop lourd (${size / (1024 * 1024)} Mo). " +
                            "Copiez le dossier Documents plutôt."
                    )
                }
            }
            // Même si statSize est inconnu (-1), on borne la lecture.
            context.contentResolver.openInputStream(uri)?.use { input ->
                return BackupSecurityArchive.readBounded(
                    input,
                    BackupSecurityArchive.MAX_ZIP_BYTES
                )
            } ?: throw BackupFailure.Io("Impossible d’ouvrir le fichier de sauvegarde.")
        } catch (e: BackupFailure) {
            throw e
        } catch (e: Exception) {
            throw BackupFailure.Io("Impossible de lire le fichier de sauvegarde.", e)
        }
    }

    private fun mapFailure(failure: BackupFailure): RestoreResult {
        return when (failure) {
            is BackupFailure.NotConfigured -> RestoreResult.NotConfigured
            is BackupFailure.UnsupportedSchema -> RestoreResult.UnsupportedSchema(failure.version)
            is BackupFailure.Integrity -> {
                // Tentative d'extraction expected/actual depuis le message, sinon Io générique.
                val match = Regex(
                    """attendu=([0-9a-fA-F]+), calculé=([0-9a-fA-F]+)"""
                ).find(failure.message ?: "")
                if (match != null) {
                    RestoreResult.InvalidChecksum(
                        expected = match.groupValues[1],
                        actual = match.groupValues[2]
                    )
                } else {
                    RestoreResult.IoError(
                        failure.message ?: "Cette copie est abîmée ou a été modifiée."
                    )
                }
            }
            is BackupFailure.Io -> RestoreResult.IoError(
                failure.message ?: "Impossible de lire cette sauvegarde."
            )
        }
    }
}
