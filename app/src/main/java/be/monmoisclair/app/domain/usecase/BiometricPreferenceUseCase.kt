package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.data.db.dao.AppMetaDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricPreferenceUseCase @Inject constructor(
    private val appMetaDao: AppMetaDao,
    private val mutatingRepository: MutatingRepository
) {
    suspend fun isEnabled(): Boolean =
        appMetaDao.get(AppMetaKeys.BIOMETRIC_ENABLED)?.value == "true"

    /**
     * Persiste le choix et déclenche un backup (sans reprogrammer les rappels).
     */
    suspend fun setEnabled(enabled: Boolean) {
        mutatingRepository.mutate(rescheduleReminders = false) {
            appMetaDao.upsert(
                AppMetaEntity(
                    AppMetaKeys.BIOMETRIC_ENABLED,
                    if (enabled) "true" else "false"
                )
            )
        }
    }
}
