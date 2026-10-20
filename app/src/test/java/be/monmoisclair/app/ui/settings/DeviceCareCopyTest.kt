package be.monmoisclair.app.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DeviceCareCopyTest {

    @Test
    fun body_mentions_sleep_without_jargon() {
        assertThat(DeviceCareCopy.BODY).contains("mise en veille")
        assertThat(DeviceCareCopy.BODY).contains("Mon Mois Clair")
        assertThat(DeviceCareCopy.TITLE).doesNotContain("Device Care")
        assertThat(DeviceCareCopy.BODY).doesNotContain("Device Care")
        assertThat(DeviceCareCopy.BODY).doesNotContain("Mise en veille profonde")
    }
}
