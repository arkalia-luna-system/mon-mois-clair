package be.monmoisclair.app.ui.format

import be.monmoisclair.app.domain.AppClock
import java.time.Instant

/** Trace rassurante « FAIT le JJ/MM/AA » (anti-doute, y compris mois passés). */
object DoneDateFormat {
    fun label(doneAtEpochMs: Long?): String? {
        if (doneAtEpochMs == null || doneAtEpochMs <= 0L) return null
        val z = Instant.ofEpochMilli(doneAtEpochMs).atZone(AppClock.zone)
        val yy = z.year % 100
        return "FAIT le %02d/%02d/%02d".format(z.dayOfMonth, z.monthValue, yy)
    }
}
