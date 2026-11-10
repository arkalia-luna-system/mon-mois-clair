package be.monmoisclair.app.data.backup

import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.BackupHealth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupHealthStore @Inject constructor(
    private val appMetaDao: AppMetaDao
) {
    suspend fun read(): BackupHealth {
        val raw = appMetaDao.get(AppMetaKeys.BACKUP_HEALTH)?.value
            ?: return BackupHealth.NOT_CONFIGURED
        return runCatching { BackupHealth.valueOf(raw) }
            .getOrDefault(BackupHealth.BROKEN)
    }

    suspend fun mark(health: BackupHealth) {
        appMetaDao.upsert(
            AppMetaEntity(
                key = AppMetaKeys.BACKUP_HEALTH,
                value = health.name
            )
        )
    }

    suspend fun markSuccess(checksumSha256: String, atEpochMs: Long) {
        mark(BackupHealth.OK)
        appMetaDao.upsert(
            AppMetaEntity(AppMetaKeys.LAST_BACKUP_CHECKSUM_SHA256, checksumSha256)
        )
        appMetaDao.upsert(
            AppMetaEntity(AppMetaKeys.LAST_BACKUP_AT_EPOCH_MS, atEpochMs.toString())
        )
    }

    suspend fun markBroken() {
        mark(BackupHealth.BROKEN)
    }

    /** Dernière copie réussie (epoch ms), ou null si jamais / inconnu. */
    suspend fun readLastBackupAtEpochMs(): Long? {
        val raw = appMetaDao.get(AppMetaKeys.LAST_BACKUP_AT_EPOCH_MS)?.value?.trim().orEmpty()
        if (raw.isEmpty() || raw == "0") return null
        return raw.toLongOrNull()?.takeIf { it > 0L }
    }
}
