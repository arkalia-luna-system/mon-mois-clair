package be.monmoisclair.app.data.reminder

import android.content.Context
import android.os.UserManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class BootReceiverCredentialStorageTest {

    @Test
    fun isCredentialStorageAvailable_true_whenUserUnlocked() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val um = context.getSystemService(UserManager::class.java)!!
        shadowOf(um).setUserUnlocked(true)
        assertThat(BootReceiver.isCredentialStorageAvailable(context)).isTrue()
    }

    @Test
    fun isCredentialStorageAvailable_false_whenUserLocked() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val um = context.getSystemService(UserManager::class.java)!!
        shadowOf(um).setUserUnlocked(false)
        assertThat(BootReceiver.isCredentialStorageAvailable(context)).isFalse()
    }
}
