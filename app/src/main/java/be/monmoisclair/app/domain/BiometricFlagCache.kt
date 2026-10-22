package be.monmoisclair.app.domain

import android.content.Context

/**
 * Miroir rapide de l’empreinte (SharedPreferences) pour poser [android.view.WindowManager.LayoutParams.FLAG_SECURE]
 * avant le premier frame — Room n’est pas encore lu.
 */
object BiometricFlagCache {
    private const val PREFS = "biometric_flag_cache"
    private const val KEY = "enabled"

    fun peek(context: Context): Boolean =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY, false)

    fun write(context: Context, enabled: Boolean) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY, enabled)
            .apply()
    }
}
