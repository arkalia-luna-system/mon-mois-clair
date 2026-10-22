package be.monmoisclair.app.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DeviceCareCopyTest {

    @Test
    fun hint_and_title_are_jargon_free() {
        assertThat(DeviceCareCopy.TITLE).doesNotContain("Device Care")
        assertThat(DeviceCareCopy.HINT).contains("mise en veille")
        assertThat(DeviceCareCopy.HINT).contains("Batterie")
        assertThat(DeviceCareCopy.HINT).contains("Mon Mois Clair")
        assertThat(DeviceCareCopy.HINT).doesNotContain("Device Care")
        assertThat(DeviceCareCopy.HINT).doesNotContain("Mise en veille profonde")
    }
}
