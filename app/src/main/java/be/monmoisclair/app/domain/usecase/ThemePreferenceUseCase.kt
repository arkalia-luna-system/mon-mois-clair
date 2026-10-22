package be.monmoisclair.app.domain.usecase

import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferenceUseCase @Inject constructor(
    private val appMetaDao: AppMetaDao,
    private val mutatingRepository: MutatingRepository
) {
    private val _preference = MutableStateFlow(ThemePreference.System)
    val preference: StateFlow<ThemePreference> = _preference.asStateFlow()

    suspend fun hydrate() {
        _preference.value = read()
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
        _preference.value = preference
    }
}
