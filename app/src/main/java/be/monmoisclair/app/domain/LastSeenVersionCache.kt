package be.monmoisclair.app.domain

import android.content.Context

/** Dernière version (versionCode) pour laquelle l’écran « Nouveautés » a été vu. */
object LastSeenVersionCache {
    private const val PREFS = "last_seen_version_cache"
    private const val KEY = "lastSeenVersionCode"

    fun peek(context: Context): Int =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY, 0)

    fun write(context: Context, versionCode: Int) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY, versionCode)
            .apply()
    }
}
