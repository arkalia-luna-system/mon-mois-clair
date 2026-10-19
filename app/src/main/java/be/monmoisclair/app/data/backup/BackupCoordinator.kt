package be.monmoisclair.app.data.backup

import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.BackupHealth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestration : snapshot DB → document JSON + checksum → écriture SAF → health.
 * Toutes les erreurs I/O remontent via [BackupFailure] et [BackupHealthStore].
 */
@Singleton
class BackupCoordinator @Inject constructor(
    private val snapshotReader: BackupSnapshotReader,
    private val codec: BackupJsonCodec,
    private val safBackupStore: SafBackupStore,
    private val healthStore: BackupHealthStore
) {

    sealed class Result {
        data class Success(val checksumSha256: String) : Result()
        data object SkippedNotConfigured : Result()
        data class Failed(val failure: BackupFailure) : Result()
    }

    suspend fun runAutoBackup(): Result {
        if (!safBackupStore.hasPersistedTree()) {
            healthStore.mark(BackupHealth.NOT_CONFIGURED)
            return Result.SkippedNotConfigured
        }
        return try {
            val payload = snapshotReader.readPayload()
            val document = codec.buildDocument(
                schemaVersion = AppMetaKeys.CURRENT_SCHEMA_VERSION.toInt(),
                exportedAtEpochMs = AppClock.nowEpochMs(),
                zoneId = AppClock.zone.id,
                payload = payload
            )
            safBackupStore.writeRotatedBackup(document)
            healthStore.markSuccess(
                checksumSha256 = document.checksumSha256,
                atEpochMs = document.exportedAtEpochMs
            )
            Result.Success(document.checksumSha256)
        } catch (e: BackupFailure) {
            if (e !is BackupFailure.NotConfigured) {
                healthStore.markBroken()
            }
            Result.Failed(e)
        } catch (e: Exception) {
            healthStore.markBroken()
            Result.Failed(BackupFailure.Io("Auto-backup inattendu", e))
        }
    }
}
