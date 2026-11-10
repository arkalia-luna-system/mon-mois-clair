package be.monmoisclair.app.ui.home

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WhatsNewCatalogTest {

    @Test
    fun load_fromAssets_matchesParse() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val loaded = WhatsNewCatalog.load(ctx)
        assertThat(loaded.releases.map { it.versionCode }).contains(146)
        assertThat(loaded.releases.map { it.versionCode }).contains(148)
    }

    @Test
    fun parse_allowsTechnicalOnlyReleaseWithoutBullets() {
        val parsed = WhatsNewCatalog.parse(
            """
            {
              "genericFallbackBullet": "Fallback",
              "releases": [
                { "versionCode": 200, "userVisible": false, "bullets": [] }
              ]
            }
            """.trimIndent()
        )

        assertThat(parsed.releases).containsExactly(
            WhatsNewCatalog.Release(
                versionCode = 200,
                bullets = emptyList(),
                userVisible = false
            )
        )
    }
}
