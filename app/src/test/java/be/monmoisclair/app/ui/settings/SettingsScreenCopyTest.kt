package be.monmoisclair.app.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SettingsScreenCopyTest {

    @Test
    fun localOnlyCopy_staysGeneric() {
        assertThat(SETTINGS_LOCAL_ONLY_A11Y.lowercase()).contains("cet appareil")
        assertThat(SETTINGS_LOCAL_ONLY_A11Y.lowercase()).doesNotContain("téléphone")
        assertThat(SETTINGS_LOCAL_ONLY_BODY.lowercase()).contains("données locales")
        assertThat(SETTINGS_LOCAL_ONLY_BODY.lowercase()).doesNotContain("téléphone")
    }
}
