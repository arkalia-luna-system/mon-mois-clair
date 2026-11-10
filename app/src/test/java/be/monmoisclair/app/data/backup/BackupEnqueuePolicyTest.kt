package be.monmoisclair.app.data.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackupEnqueuePolicyTest {

    @Test
    fun deferWhileRunning() {
        assertThat(BackupEnqueuePolicy.shouldDeferWhileWorkerRunning(true)).isTrue()
        assertThat(BackupEnqueuePolicy.shouldDeferWhileWorkerRunning(false)).isFalse()
    }

    @Test
    fun followUpWhenCoalesced() {
        assertThat(BackupEnqueuePolicy.needsFollowUpAfterRun(true)).isTrue()
        assertThat(BackupEnqueuePolicy.needsFollowUpAfterRun(false)).isFalse()
    }
}
