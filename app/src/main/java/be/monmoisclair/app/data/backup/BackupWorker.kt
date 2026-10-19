package be.monmoisclair.app.data.backup

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val coordinator: BackupCoordinator
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return when (val outcome = coordinator.runAutoBackup()) {
            is BackupCoordinator.Result.Success -> Result.success()
            BackupCoordinator.Result.SkippedNotConfigured -> Result.success()
            is BackupCoordinator.Result.Failed -> {
                Log.e(TAG, "Auto-backup échoué: ${outcome.failure.message}", outcome.failure)
                // Pas de retry agressif : l'UI lira backupHealth=BROKEN.
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "BackupWorker"
    }
}
