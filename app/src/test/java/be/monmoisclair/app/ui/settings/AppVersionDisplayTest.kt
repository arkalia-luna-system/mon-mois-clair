package be.monmoisclair.app.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppVersionDisplayTest {

    @Test
    fun stepLabel_stripsGradlePrefix() {
        assertThat(AppVersionDisplay.stepLabel("0.1.0-6.22")).isEqualTo("6.22")
        assertThat(AppVersionDisplay.stepLabel("0.1.0-6.23")).isEqualTo("6.23")
    }

    @Test
    fun stepLabel_nullOrPlain_returnsNull() {
        assertThat(AppVersionDisplay.stepLabel(null)).isNull()
        assertThat(AppVersionDisplay.stepLabel("")).isNull()
        assertThat(AppVersionDisplay.stepLabel("6.22")).isNull()
    }
}
