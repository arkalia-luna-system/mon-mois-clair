package be.monmoisclair.app.data.reminder

import be.monmoisclair.app.domain.AppClock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZonedDateTime

/**
 * Double détente : J−3 à 10:00 et Jour J à 09:00 (Europe/Brussels).
 * Retourne null si le déclenchement est déjà passé (ne pas planifier le passé).
 */
object ReminderTiming {

    val J3_TIME: LocalTime = LocalTime.of(10, 0)
    val JD_TIME: LocalTime = LocalTime.of(9, 0)

    fun dueDate(yearMonth: YearMonth, dayOfMonth: Int): LocalDate =
        yearMonth.atDay(dayOfMonth)

    fun j3Instant(
        yearMonth: YearMonth,
        dayOfMonth: Int,
        now: ZonedDateTime = AppClock.now()
    ): Instant? {
        val due = dueDate(yearMonth, dayOfMonth)
        val trigger = due.minusDays(3).atTime(J3_TIME).atZone(AppClock.zone)
        return trigger.toInstant().takeIf { trigger.isAfter(now) }
    }

    fun jdInstant(
        yearMonth: YearMonth,
        dayOfMonth: Int,
        now: ZonedDateTime = AppClock.now()
    ): Instant? {
        val due = dueDate(yearMonth, dayOfMonth)
        val trigger = due.atTime(JD_TIME).atZone(AppClock.zone)
        return trigger.toInstant().takeIf { trigger.isAfter(now) }
    }

    fun yearMonthFromLedgerId(ledgerId: String): YearMonth = YearMonth.parse(ledgerId)
}
