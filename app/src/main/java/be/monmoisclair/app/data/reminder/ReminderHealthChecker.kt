package be.monmoisclair.app.data.reminder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import be.monmoisclair.app.data.backup.BackupHealthStore
import be.monmoisclair.app.domain.model.BackupHealth
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ReminderHealthReport(
    val notificationsGranted: Boolean,
    val exactAlarmGranted: Boolean,
    val batteryOptimizationIgnored: Boolean,
    val safBackupOk: Boolean,
    /** Toujours pertinent sur Samsung : Device Care ≠ exemption batterie standard. */
    val samsungDeviceCareHintNeeded: Boolean
) {
    val isFullyHealthy: Boolean
        get() = notificationsGranted &&
            exactAlarmGranted &&
            batteryOptimizationIgnored &&
            safBackupOk
}

@Singleton
class ReminderHealthChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmGateway: ExactAlarmGateway,
    private val backupHealthStore: BackupHealthStore
) {

    suspend fun check(): ReminderHealthReport {
        val backupOk = backupHealthStore.read() == BackupHealth.OK
        return ReminderHealthReport(
            notificationsGranted = areNotificationsGranted(),
            exactAlarmGranted = alarmGateway.canScheduleExactAlarms(),
            batteryOptimizationIgnored = isIgnoringBatteryOptimizations(),
            safBackupOk = backupOk,
            samsungDeviceCareHintNeeded = isSamsungDevice()
        )
    }

    fun areNotificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isIgnoringBatteryOptimizations(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun isSamsungDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.orEmpty().lowercase()
        val brand = Build.BRAND.orEmpty().lowercase()
        return manufacturer.contains("samsung") || brand.contains("samsung")
    }

    @SuppressLint("BatteryLife")
    fun intentIgnoreBatteryOptimizations(): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    fun intentExactAlarmSettings(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }

    fun intentAppNotificationSettings(): Intent {
        return Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    }

    /** Liste système des apps exemptées / à optimiser (proche Device Care). */
    fun intentBatteryOptimizationList(): Intent =
        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

    fun intentApplicationDetails(): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
}
