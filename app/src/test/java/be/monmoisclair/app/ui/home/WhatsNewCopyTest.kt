package be.monmoisclair.app.ui.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WhatsNewCopyTest {

    @Test
    fun payload_nullWhenAlreadySeen() {
        assertThat(WhatsNewCopy.payload(147, 147)).isNull()
        assertThat(WhatsNewCopy.payload(200, 147)).isNull()
    }

    @Test
    fun payload_skipsInvalidCurrent() {
        assertThat(WhatsNewCopy.payload(0, 0)).isNull()
        assertThat(WhatsNewCopy.payload(100, -1)).isNull()
    }

    @Test
    fun payload_mergesReleases_upToSixBullets() {
        val p = WhatsNewCopy.payload(145, 147)
        requireNotNull(p)
        assertThat(p.bullets).hasSize(WhatsNewCopy.MAX_BULLETS)
        assertThat(p.bullets.joinToString(" ")).contains("Pour aujourd’hui")
        assertThat(p.bullets.joinToString(" ")).contains("Appliquer aussi aux mois suivants")
    }

    @Test
    fun payload_onlyNewerThanLastSeen() {
        val p = WhatsNewCopy.payload(146, 147)
        requireNotNull(p)
        assertThat(p.bullets.joinToString(" ")).contains("mise à jour")
        assertThat(p.bullets.joinToString(" ")).doesNotContain("filtres Tous")
    }

    @Test
    fun releases_trackKnownVersionCodes() {
        assertThat(WhatsNewCopy.releases.map { it.versionCode }).containsExactly(146, 147)
    }
}
