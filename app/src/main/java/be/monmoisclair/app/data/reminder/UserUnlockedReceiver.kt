package be.monmoisclair.app.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import be.monmoisclair.app.data.device.CredentialStorageGate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Après déverrouillage utilisateur : reprogramme les alarmes (même garde CE que [BootReceiver]).
 */
@AndroidEntryPoint
class UserUnlockedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_USER_UNLOCKED) return
        if (!CredentialStorageGate.isAvailable(context)) {
            Log.i(TAG, "USER_UNLOCKED mais CE indisponible — skip")
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                reminderScheduler.reconcileAll()
            } catch (e: Exception) {
                Log.e(TAG, "reconcile après USER_UNLOCKED", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "UserUnlockedRcvr"
    }
}
