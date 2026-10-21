package be.monmoisclair.app.ui.format

import be.monmoisclair.app.domain.AppClock
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class FeeEventDateFormatTest {

    private val brussels = ZoneId.of("Europe/Brussels")

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
    }

    @Test
    fun label_uses_day_month_year2() {
        AppClock.clock = Clock.fixed(Instant.parse("2026-10-21T10:00:00Z"), brussels)
        val ms = Instant.parse("2026-10-21T10:00:00Z").toEpochMilli()
        assertThat(FeeEventDateFormat.label(ms)).isEqualTo("21/10/26")
    }
}
