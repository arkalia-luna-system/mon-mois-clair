package be.monmoisclair.app.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SystemSettingsMessagesTest {

    @Test
    fun openFailed_staysGenericAndActionable() {
        val msg = SystemSettingsMessages.OPEN_FAILED

        assertThat(msg).contains("réglages")
        assertThat(msg).contains("revenez")
        assertThat(msg.lowercase()).doesNotContain("téléphone")
    }

    @Test
    fun screenOpenFailed_staysActionable() {
        val msg = SystemSettingsMessages.SCREEN_OPEN_FAILED

        assertThat(msg).contains("écran système")
        assertThat(msg).contains("revenez")
        assertThat(msg.lowercase()).doesNotContain("réessayez")
    }
}
