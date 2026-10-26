package be.monmoisclair.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/** Smoke instrumenté minimal — package + process app. */
@RunWith(AndroidJUnit4::class)
class AppContextSmokeTest {

    @Test
    fun targetContext_hasExpectedPackage() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        assertThat(ctx.packageName).isEqualTo("be.monmoisclair.app")
    }
}
