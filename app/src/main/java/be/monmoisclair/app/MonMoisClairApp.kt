package be.monmoisclair.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import be.monmoisclair.app.data.reminder.PaymentNotificationHelper
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class MonMoisClairApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var paymentNotificationHelper: PaymentNotificationHelper

    override fun onCreate() {
        super.onCreate()
        paymentNotificationHelper.ensureChannel()
        sweepOrphanCameraJpegs()
    }

    /** Anciens `receipt_*.jpg` à la racine du cache (avant sous-dossier camera/). */
    private fun sweepOrphanCameraJpegs() {
        runCatching {
            cacheDir.listFiles()?.forEach { f ->
                if (f.isFile && f.name.startsWith("receipt_") && f.name.endsWith(".jpg")) {
                    f.delete()
                }
            }
            File(cacheDir, "camera").listFiles()?.forEach { f ->
                // Ne garde pas des JPEG orphelins > 1 h (capture abandonnée).
                if (f.isFile &&
                    f.name.startsWith("receipt_") &&
                    f.name.endsWith(".jpg") &&
                    System.currentTimeMillis() - f.lastModified() > 60L * 60L * 1000L
                ) {
                    f.delete()
                }
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
