package be.monmoisclair.app.data.reminder

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReminderHealthReportTest {

    @Test
    fun blocked_onlyWhenNotificationsOrExactAlarmsMissing() {
        val ok = report(
            notificationsGranted = true,
            exactAlarmGranted = true,
            batteryOptimizationIgnored = false
        )
        assertThat(ok.remindersBlocked).isFalse()
        assertThat(ok.isFullyHealthy).isFalse()

        assertThat(
            report(notificationsGranted = false, exactAlarmGranted = true).remindersBlocked
        ).isTrue()
        assertThat(
            report(notificationsGranted = true, exactAlarmGranted = false).remindersBlocked
        ).isTrue()
    }

    private fun report(
        notificationsGranted: Boolean,
        exactAlarmGranted: Boolean,
        batteryOptimizationIgnored: Boolean = true
    ) = ReminderHealthReport(
        notificationsGranted = notificationsGranted,
        exactAlarmGranted = exactAlarmGranted,
        batteryOptimizationIgnored = batteryOptimizationIgnored,
        safBackupOk = true,
        samsungDeviceCareHintNeeded = true
    )
}
