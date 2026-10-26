package be.monmoisclair.app.data.reminder

import be.monmoisclair.app.domain.AppClock
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class ReminderTimingTest {

    private val brussels = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        // 1er octobre 2026 12:00 Bruxelles
        freeze("2026-10-01T12:00:00+02:00")
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
    }

    @Test
    fun j3_forDay5_isOctober2At10h() {
        val instant = ReminderTiming.j3Instant(YearMonth.of(2026, 10), 5)!!
        val zdt = instant.atZone(brussels)
        assertThat(zdt.toLocalDate()).isEqualTo(LocalDate.of(2026, 10, 2))
        assertThat(zdt.toLocalTime()).isEqualTo(ReminderTiming.J3_TIME)
    }

    @Test
    fun jd_forDay5_isOctober5At9h() {
        val instant = ReminderTiming.jdInstant(YearMonth.of(2026, 10), 5)!!
        val zdt = instant.atZone(brussels)
        assertThat(zdt.toLocalDate()).isEqualTo(LocalDate.of(2026, 10, 5))
        assertThat(zdt.toLocalTime()).isEqualTo(ReminderTiming.JD_TIME)
    }

    @Test
    fun j3_returnsNull_whenAlreadyPast() {
        // Le 4 octobre : J−3 du 5 (2 oct 10h) est passé
        freeze("2026-10-04T11:00:00+02:00")
        assertThat(ReminderTiming.j3Instant(YearMonth.of(2026, 10), 5)).isNull()
        assertThat(ReminderTiming.jdInstant(YearMonth.of(2026, 10), 5)).isNotNull()
    }

    @Test
    fun jd_returnsNull_whenAlreadyPast() {
        freeze("2026-10-05T10:00:00+02:00")
        assertThat(ReminderTiming.jdInstant(YearMonth.of(2026, 10), 5)).isNull()
    }

    @Test
    fun j3_earlyMonth_canBePreviousMonth() {
        // Échéance le 2 → J−3 = 29 septembre 10h
        freeze("2026-09-20T08:00:00+02:00")
        val instant = ReminderTiming.j3Instant(YearMonth.of(2026, 10), 2)!!
        val zdt = instant.atZone(brussels)
        assertThat(zdt.toLocalDate()).isEqualTo(LocalDate.of(2026, 9, 29))
        assertThat(zdt.hour).isEqualTo(10)
    }

    @Test
    fun dueDate_clampsDay31_inFebruary() {
        // P3-R01 : ne pas lever DateTimeException
        assertThat(ReminderTiming.dueDate(YearMonth.of(2025, 2), 31))
            .isEqualTo(LocalDate.of(2025, 2, 28))
        assertThat(ReminderTiming.dueDate(YearMonth.of(2024, 2), 31))
            .isEqualTo(LocalDate.of(2024, 2, 29))
        // Échéance février 2026 déjà passée au 1er oct → J−3 null
        assertThat(ReminderTiming.j3Instant(YearMonth.of(2026, 2), 31)).isNull()
    }

    private fun freeze(offsetDateTime: String) {
        val instant = java.time.OffsetDateTime.parse(offsetDateTime).toInstant()
        AppClock.clock = Clock.fixed(instant, brussels)
    }
}
