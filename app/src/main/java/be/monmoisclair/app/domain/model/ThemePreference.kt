package be.monmoisclair.app.domain.model

/**
 * Choix d’apparence (Réglages).
 * Défaut = [System] (comme avant 6.46 : suit One UI).
 */
enum class ThemePreference(val storageValue: String) {
    System("system"),
    Light("light"),
    Dark("dark");

    /** Nuit effective selon le mode système. */
    fun resolveNight(systemDark: Boolean): Boolean = when (this) {
        System -> systemDark
        Light -> false
        Dark -> true
    }

    companion object {
        fun fromStorage(raw: String?): ThemePreference {
            val key = raw?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it.storageValue == key } ?: System
        }
    }
}
