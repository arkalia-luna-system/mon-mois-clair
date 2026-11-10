package be.monmoisclair.app.domain

import android.content.Context

/** Rappels ponctuels accueil (sauvegarde, tip post-mise à jour) — hors Room. */
object StartupNudgeCache {
    private const val PREFS = "startup_nudge_cache"
    private const val KEY_BACKUP = "backupNudgeDismissed"
    private const val KEY_HELP_TIP = "postWhatsNewHelpTipShown"

    fun backupNudgeDismissed(context: Context): Boolean =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_BACKUP, false)

    fun dismissBackupNudge(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BACKUP, true)
            .apply()
    }

    fun postWhatsNewHelpTipShown(context: Context): Boolean =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_HELP_TIP, false)

    fun markPostWhatsNewHelpTipShown(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HELP_TIP, true)
            .apply()
    }
}
