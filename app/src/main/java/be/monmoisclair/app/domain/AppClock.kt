package be.monmoisclair.app.domain

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

object AppClock {
    val zone: ZoneId = ZoneId.of("Europe/Brussels")

    @Volatile
    var clock: Clock = Clock.system(zone)

    fun today(): LocalDate = LocalDate.now(clock)

    fun now(): ZonedDateTime = ZonedDateTime.now(clock)

    fun nowEpochMs(): Long = Instant.now(clock).toEpochMilli()

    fun currentYearMonth(): YearMonth = YearMonth.from(today())

    fun ledgerId(ym: YearMonth = currentYearMonth()): String = ym.toString()
}
