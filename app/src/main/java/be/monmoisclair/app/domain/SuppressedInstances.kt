package be.monmoisclair.app.domain

import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.domain.model.AppMetaKeys

/**
 * Tombstones légers (sans migration Room) : une ligne liée à un modèle
 * supprimée « ce mois seulement » ne doit pas être réinjectée par
 * [MonthCycleEngine.syncMissingTemplates].
 *
 * Stockage : ids séparés par `\n` dans `app_meta` (inclus backup/restore).
 */
object SuppressedInstances {
    suspend fun contains(appMetaDao: AppMetaDao, instanceId: String): Boolean =
        read(appMetaDao).contains(instanceId)

    suspend fun add(appMetaDao: AppMetaDao, instanceId: String) {
        val id = instanceId.trim()
        if (id.isEmpty()) return
        val next = read(appMetaDao) + id
        write(appMetaDao, next)
    }

    suspend fun read(appMetaDao: AppMetaDao): Set<String> {
        val raw = appMetaDao.get(AppMetaKeys.SUPPRESSED_INSTANCE_IDS)?.value.orEmpty()
        if (raw.isBlank()) return emptySet()
        return raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    private suspend fun write(appMetaDao: AppMetaDao, ids: Set<String>) {
        appMetaDao.upsert(
            AppMetaEntity(
                AppMetaKeys.SUPPRESSED_INSTANCE_IDS,
                ids.sorted().joinToString("\n")
            )
        )
    }
}
