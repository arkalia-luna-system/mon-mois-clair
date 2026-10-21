package be.monmoisclair.app.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reprogramme les alarmes après :
 * - boot téléphone
 * - **mise à jour APK** (`MY_PACKAGE_REPLACED`) — T1
 * - changement d’heure / fuseau (été-hiver) — T1
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action !in RELEVANT_ACTIONS) return
        // TIME_SET peut spammer (réglage horloge) : debounce 2 min.
        if (action == Intent.ACTION_TIME_CHANGED && !allowTimeReconcile(context)) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                reminderScheduler.reconcileAll()
            } catch (e: Exception) {
                Log.e(TAG, "reconcileAll échoué (action=$action)", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
        private const val PREFS = "boot_receiver"
        private const val KEY_LAST_TIME_RECONCILE = "last_time_reconcile_ms"
        private const val TIME_DEBOUNCE_MS = 2L * 60L * 1000L

        private val RELEVANT_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )

        private fun allowTimeReconcile(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            val last = prefs.getLong(KEY_LAST_TIME_RECONCILE, 0L)
            if (now - last < TIME_DEBOUNCE_MS) return false
            prefs.edit().putLong(KEY_LAST_TIME_RECONCILE, now).apply()
            return true
        }
    }
}
