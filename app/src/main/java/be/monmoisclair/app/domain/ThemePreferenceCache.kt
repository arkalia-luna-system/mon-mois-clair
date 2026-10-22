package be.monmoisclair.app.domain

import android.content.Context
import be.monmoisclair.app.domain.model.ThemePreference

/**
 * Miroir rapide de [ThemePreference] (SharedPreferences) pour le 1er frame
 * avant hydrate Room — évite le flash Clair/Sombre.
 */
object ThemePreferenceCache {
    private const val PREFS = "theme_preference_cache"
    private const val KEY = "mode"

    fun peek(context: Context): ThemePreference {
        val raw = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null)
        return ThemePreference.fromStorage(raw)
    }

    fun write(context: Context, preference: ThemePreference) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, preference.storageValue)
            .apply()
    }
}
