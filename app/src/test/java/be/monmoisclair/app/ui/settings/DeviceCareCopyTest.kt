package be.monmoisclair.app.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DeviceCareCopyTest {

    @Test
    fun body_mentions_sleep_limits() {
        assertThat(DeviceCareCopy.BODY).contains("Mise en veille")
        assertThat(DeviceCareCopy.BODY).contains("Mise en veille profonde")
        assertThat(DeviceCareCopy.TITLE).contains("Device Care")
    }
}
