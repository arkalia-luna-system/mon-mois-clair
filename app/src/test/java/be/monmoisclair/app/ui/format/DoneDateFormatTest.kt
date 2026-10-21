package be.monmoisclair.app.ui.format

import be.monmoisclair.app.domain.AppClock
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class DoneDateFormatTest {

    private val zone: ZoneId = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        AppClock.clock = Clock.fixed(Instant.parse("2026-10-04T12:00:00Z"), zone)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(zone)
    }

    @Test
    fun null_returnsNull() {
        assertThat(DoneDateFormat.label(null)).isNull()
    }

    @Test
    fun formatsDayMonthInBrussels() {
        val ms = Instant.parse("2026-10-04T10:00:00Z").toEpochMilli()
        assertThat(DoneDateFormat.label(ms)).isEqualTo("FAIT le 04/10/26")
    }
}
