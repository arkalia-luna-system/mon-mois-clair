package be.monmoisclair.app.domain.usecase

import android.content.Context
import be.monmoisclair.app.data.crypto.KeystoreAesKey
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.BiometricFlagCache
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.data.db.dao.AppMetaDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricPreferenceUseCase @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appMetaDao: AppMetaDao,
    private val mutatingRepository: MutatingRepository
) {
    /** Lecture sync du miroir SharedPreferences (avant hydrate Room). */
    fun cachedEnabled(): Boolean = BiometricFlagCache.peek(appContext)

    suspend fun isEnabled(): Boolean {
        val enabled = appMetaDao.get(AppMetaKeys.BIOMETRIC_ENABLED)?.value == "true"
        BiometricFlagCache.write(appContext, enabled)
        return enabled
    }

    /**
     * Persiste le choix et déclenche un backup (sans reprogrammer les rappels).
     * Active : provisionne la clé Keystore [KeystoreAesKey.UNLOCK_ALIAS] pour CryptoObject.
     */
    suspend fun setEnabled(enabled: Boolean) {
        if (enabled) {
            KeystoreAesKey.getOrCreateUnlockKey()
        }
        BiometricFlagCache.write(appContext, enabled)
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
