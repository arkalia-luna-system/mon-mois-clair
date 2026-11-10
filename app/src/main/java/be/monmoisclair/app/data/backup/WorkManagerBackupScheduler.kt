package be.monmoisclair.app.data.backup

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerBackupScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : BackupScheduler {

    private val coalesceWhileRunning = AtomicBoolean(false)

    override fun enqueueImmediate() {
        val wm = WorkManager.getInstance(context)
        val running = wm.getWorkInfosForUniqueWork(UNIQUE_WORK_NAME).get()
            .any { it.state == WorkInfo.State.RUNNING }
        if (BackupEnqueuePolicy.shouldDeferWhileWorkerRunning(running)) {
            coalesceWhileRunning.set(true)
            return
        }
        val request = OneTimeWorkRequestBuilder<BackupWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        wm.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** Après un backup réussi : rejouer si des mutations ont été ignorées pendant l’exécution. */
    fun enqueueFollowUpIfCoalesced() {
        if (BackupEnqueuePolicy.needsFollowUpAfterRun(coalesceWhileRunning.getAndSet(false))) {
            enqueueImmediate()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "mon_mois_clair_auto_backup"
    }
}
