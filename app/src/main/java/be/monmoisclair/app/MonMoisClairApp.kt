package be.monmoisclair.app

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import be.monmoisclair.app.data.reminder.PaymentNotificationHelper
import be.monmoisclair.app.data.widget.CarnetWidgetRefresher
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class MonMoisClairApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var paymentNotificationHelper: PaymentNotificationHelper

    @Inject
    lateinit var widgetRefresher: CarnetWidgetRefresher

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var lastWidgetConfigRefreshMs = 0L

    private val configReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (!::widgetRefresher.isInitialized) return
            // Samsung envoie CONFIGURATION_CHANGED en rafale (thème, fold, AOD) —
            // debounce pour éviter wake DB / batterie Device Care.
            val now = System.currentTimeMillis()
            if (now - lastWidgetConfigRefreshMs < WIDGET_CONFIG_DEBOUNCE_MS) return
            lastWidgetConfigRefreshMs = now
            widgetScope.launch { runCatching { widgetRefresher.refresh() } }
        }
    }

    override fun onCreate() {
        super.onCreate()
        paymentNotificationHelper.ensureChannel()
        sweepOrphanCameraJpegs()
        val filter = IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(configReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(configReceiver, filter)
        }
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

    companion object {
        private const val WIDGET_CONFIG_DEBOUNCE_MS = 15_000L
    }
}
