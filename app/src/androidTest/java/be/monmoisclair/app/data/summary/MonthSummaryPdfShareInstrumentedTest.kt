package be.monmoisclair.app.data.summary

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** PDF résumé : cache/share éphémère + wipe (device réel). */
@RunWith(AndroidJUnit4::class)
class MonthSummaryPdfShareInstrumentedTest {

    @Test
    fun prepareAndWipe_onDeviceCache() {
        val cache = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val out = MonthSummaryPdfShare.prepareTarget(cache)
        out.writeBytes("%PDF-1.4 smoke".toByteArray())
        assertThat(out.exists()).isTrue()
        assertThat(out.parentFile!!.name).isEqualTo(MonthSummaryPdfShare.SHARE_SUBDIR)

        val wiped = MonthSummaryPdfShare.wipeSharePdfs(cache)
        assertThat(wiped).isAtLeast(1)
        assertThat(File(out.absolutePath).exists()).isFalse()
    }
}
