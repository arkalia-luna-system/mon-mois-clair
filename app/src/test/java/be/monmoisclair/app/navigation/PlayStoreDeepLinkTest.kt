package be.monmoisclair.app.navigation

import android.content.Intent
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlayStoreDeepLinkTest {

    private val pkg = "be.monmoisclair.app"

    @Test
    fun rejectsWrongPackage_onlyWhenOtherApp() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=com.other.app")
        )
        assertThat(PlayStoreDeepLink.rejectsWrongPackage(intent, pkg)).isTrue()
    }

    @Test
    fun acceptsOwnPackage_orMissingId() {
        val ours = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
        )
        assertThat(PlayStoreDeepLink.rejectsWrongPackage(ours, pkg)).isFalse()
        val noId = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details")
        )
        assertThat(PlayStoreDeepLink.rejectsWrongPackage(noId, pkg)).isFalse()
        assertThat(PlayStoreDeepLink.rejectsWrongPackage(Intent(Intent.ACTION_MAIN), pkg)).isFalse()
    }
}
