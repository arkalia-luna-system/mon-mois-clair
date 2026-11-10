package be.monmoisclair.app.ui.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class WhatsNewCopyTest {

    private val catalog: WhatsNewCatalog.Catalog by lazy {
        val file = File("src/main/assets/whats_new/catalog.json")
        WhatsNewCatalog.parse(file.readText())
    }

    private val releases get() = catalog.releases

    @Test
    fun payload_nullWhenAlreadySeen() {
        assertThat(
            WhatsNewCopy.payloadForReleases(147, 147, releases, catalog.genericFallbackBullet)
        ).isNull()
        assertThat(
            WhatsNewCopy.payloadForReleases(200, 147, releases, catalog.genericFallbackBullet)
        ).isNull()
    }

    @Test
    fun payload_skipsInvalidCurrent() {
        assertThat(
            WhatsNewCopy.payloadForReleases(0, 0, releases, catalog.genericFallbackBullet)
        ).isNull()
        assertThat(
            WhatsNewCopy.payloadForReleases(100, -1, releases, catalog.genericFallbackBullet)
        ).isNull()
    }

    @Test
    fun payload_mergesReleases_upToSixBullets() {
        val p = WhatsNewCopy.payloadForReleases(145, 147, releases, catalog.genericFallbackBullet)
        requireNotNull(p)
        assertThat(p.bullets).hasSize(WhatsNewCopy.MAX_BULLETS)
        assertThat(p.bullets.joinToString(" ")).contains("Pour aujourd’hui")
        assertThat(p.bullets.joinToString(" ")).contains("Appliquer aussi aux mois suivants")
    }

    @Test
    fun payload_onlyNewerThanLastSeen() {
        val p = WhatsNewCopy.payloadForReleases(146, 147, releases, catalog.genericFallbackBullet)
        requireNotNull(p)
        assertThat(p.bullets.joinToString(" ")).contains("mise à jour")
        assertThat(p.bullets.joinToString(" ")).doesNotContain("filtres Tous")
    }

    @Test
    fun payload_genericFallbackWhenNoReleaseEntry() {
        val p = WhatsNewCopy.payloadForReleases(
            154,
            155,
            releases,
            catalog.genericFallbackBullet,
        )
        requireNotNull(p)
        assertThat(p.bullets).containsExactly(catalog.genericFallbackBullet)
    }

    @Test
    fun versionLabelFromName() {
        assertThat(WhatsNewCopy.versionLabelFromName("0.1.0-7.42")).isEqualTo("Version 7.42")
        assertThat(WhatsNewCopy.versionLabelFromName(null)).isNull()
    }

    @Test
    fun catalog_hasEntryForShippedVersionCode() {
        assertThat(releases.map { it.versionCode }).contains(154)
    }

    @Test
    fun catalog_parseAssetFile() {
        val file = File("src/main/assets/whats_new/catalog.json")
        assertThat(file.isFile).isTrue()
        val parsed = WhatsNewCatalog.parse(file.readText())
        assertThat(parsed.releases).isNotEmpty()
        assertThat(parsed.genericFallbackBullet).isNotEmpty()
    }
}
