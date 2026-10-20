package be.monmoisclair.app.data.reminder

import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.MonthDayClamp
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

    /** Jour d’échéance clampé (31 en février → 28/29) — P3-R01. */
    fun dueDate(yearMonth: YearMonth, dayOfMonth: Int): LocalDate {
        val day = MonthDayClamp.clamp(dayOfMonth.coerceIn(1, 31), yearMonth)
        return yearMonth.atDay(day)
    }

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

    val OVERDUE_TIME: LocalTime = LocalTime.of(10, 0)

    /**
     * Prochaine relance overdue à 10:00 Brussels (aujourd’hui si encore avant 10h, sinon demain).
     */
    fun nextOverdueInstant(now: ZonedDateTime = AppClock.now()): Instant {
        var day = now.toLocalDate()
        var trigger = day.atTime(OVERDUE_TIME).atZone(AppClock.zone)
        if (!trigger.isAfter(now)) {
            day = day.plusDays(1)
            trigger = day.atTime(OVERDUE_TIME).atZone(AppClock.zone)
        }
        return trigger.toInstant()
    }

    fun yearMonthFromLedgerId(ledgerId: String): YearMonth = YearMonth.parse(ledgerId)
}
