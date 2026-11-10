package be.monmoisclair.app.ui.camera

import android.content.Intent
import android.provider.Settings
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppDetailsSettingsIntentTest {

    @Test
    fun buildsAppDetailsIntentForPackage() {
        val intent = appDetailsSettingsIntent("be.monmoisclair.app")

        assertThat(intent.action).isEqualTo(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        assertThat(intent.dataString).isEqualTo("package:be.monmoisclair.app")
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
            .isEqualTo(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    fun reportsOpenFailureWithoutThrowing() {
        val opened = openAppDetailsSettings("be.monmoisclair.app") {
            throw IllegalStateException("boom")
        }

        assertThat(opened).isFalse()
    }

    @Test
    fun reportsOpenSuccess() {
        var launchedIntent: Intent? = null

        val opened = openAppDetailsSettings("be.monmoisclair.app") { launchedIntent = it }

        assertThat(opened).isTrue()
        assertThat(launchedIntent?.action).isEqualTo(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    }
}
