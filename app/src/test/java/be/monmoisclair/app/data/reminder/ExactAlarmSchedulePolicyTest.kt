package be.monmoisclair.app.data.reminder

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExactAlarmSchedulePolicyTest {

    @Test
    fun cancelWhenPermissionMissing() {
        assertThat(ExactAlarmSchedulePolicy.shouldCancelWhenExactPermissionMissing(false)).isTrue()
        assertThat(ExactAlarmSchedulePolicy.shouldCancelWhenExactPermissionMissing(true)).isFalse()
    }

    @Test
    fun cancelAfterSecurityException() {
        assertThat(ExactAlarmSchedulePolicy.shouldCancelAfterScheduleSecurityFailure()).isTrue()
    }
}
