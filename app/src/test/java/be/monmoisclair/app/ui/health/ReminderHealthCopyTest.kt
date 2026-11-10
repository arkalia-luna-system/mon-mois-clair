package be.monmoisclair.app.ui.health

import be.monmoisclair.app.data.reminder.ReminderHealthReport
import be.monmoisclair.app.ui.components.CarnetBannerKind
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReminderHealthCopyTest {

    @Test
    fun topBanner_nullWhenHealthy() {
        val report = ReminderHealthReport(
            notificationsGranted = true,
            exactAlarmGranted = true,
            batteryOptimizationIgnored = true,
            safBackupOk = true,
            samsungDeviceCareHintNeeded = false,
        )
        assertThat(ReminderHealthCopy.topBanner(report)).isNull()
    }

    @Test
    fun topBanner_notificationsAndAlarm() {
        val report = ReminderHealthReport(
            notificationsGranted = false,
            exactAlarmGranted = false,
            batteryOptimizationIgnored = true,
            safBackupOk = true,
            samsungDeviceCareHintNeeded = false,
        )
        val banner = ReminderHealthCopy.topBanner(report)
        assertThat(banner).isNotNull()
        assertThat(banner).contains("notifications")
        assertThat(banner).contains("réveil")
    }

    @Test
    fun topBanner_safBackup() {
        val report = ReminderHealthReport(
            notificationsGranted = true,
            exactAlarmGranted = true,
            batteryOptimizationIgnored = true,
            safBackupOk = false,
            samsungDeviceCareHintNeeded = false,
        )
        assertThat(ReminderHealthCopy.topBanner(report)).contains("copie")
    }

    @Test
    fun topBanner_batteryOnly() {
        val report = ReminderHealthReport(
            notificationsGranted = true,
            exactAlarmGranted = true,
            batteryOptimizationIgnored = false,
            safBackupOk = true,
            samsungDeviceCareHintNeeded = true,
        )
        assertThat(ReminderHealthCopy.topBanner(report)).contains("Batterie")
    }

    @Test
    fun topBannerKind_errorWhenRemindersBlocked() {
        val report = ReminderHealthReport(
            notificationsGranted = false,
            exactAlarmGranted = true,
            batteryOptimizationIgnored = true,
            safBackupOk = true,
            samsungDeviceCareHintNeeded = false,
        )
        assertThat(ReminderHealthCopy.topBannerKind(report)).isEqualTo(CarnetBannerKind.Error)
    }

    @Test
    fun topBannerKind_warnForBatteryNotBlocked() {
        val report = ReminderHealthReport(
            notificationsGranted = true,
            exactAlarmGranted = true,
            batteryOptimizationIgnored = false,
            safBackupOk = true,
            samsungDeviceCareHintNeeded = false,
        )
        assertThat(ReminderHealthCopy.topBannerKind(report)).isEqualTo(CarnetBannerKind.Warn)
    }
}
