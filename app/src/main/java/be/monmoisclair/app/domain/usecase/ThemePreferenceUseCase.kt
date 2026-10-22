package be.monmoisclair.app.domain.usecase

import android.content.Context
import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.ThemePreferenceCache
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.ThemePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferenceUseCase @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appMetaDao: AppMetaDao,
    private val mutatingRepository: MutatingRepository
) {
    private val _preference = MutableStateFlow(ThemePreferenceCache.peek(appContext))
    val preference: StateFlow<ThemePreference> = _preference.asStateFlow()

    suspend fun hydrate() {
        val stored = read()
        ThemePreferenceCache.write(appContext, stored)
        _preference.value = stored
    }

    suspend fun read(): ThemePreference =
        ThemePreference.fromStorage(appMetaDao.get(AppMetaKeys.THEME_PREFERENCE)?.value)

    /**
     * Persiste le choix et déclenche un backup (sans reprogrammer les rappels).
     */
    suspend fun set(preference: ThemePreference) {
        mutatingRepository.mutate(rescheduleReminders = false) {
            appMetaDao.upsert(
                AppMetaEntity(AppMetaKeys.THEME_PREFERENCE, preference.storageValue)
            )
        }
        ThemePreferenceCache.write(appContext, preference)
        _preference.value = preference
    }
}
