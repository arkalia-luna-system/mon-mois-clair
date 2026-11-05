package be.monmoisclair.app.update

import android.app.Activity
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Sans Play Store (Robolectric) : IAU Immediate = no-op, pas de crash, cold start une seule fois.
 */
@RunWith(RobolectricTestRunner::class)
class PlayInAppUpdaterTest {

    @Test
    fun coldStart_sideloadOrNoPlay_isNoOpAndIdempotent() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val updater = PlayInAppUpdater(context)
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()

        updater.maybeCheckImmediateOnColdStart(activity)
        updater.maybeCheckImmediateOnColdStart(activity)
        updater.onResumeContinueIfNeeded(activity)

        assertThat(PlayInAppUpdater.REQUEST_CODE).isEqualTo(7819)
    }
}
