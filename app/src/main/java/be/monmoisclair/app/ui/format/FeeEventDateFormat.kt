package be.monmoisclair.app.ui.format

import be.monmoisclair.app.domain.AppClock
import java.time.Instant

/** Date courte pour une lettre de frais : « 21/10/26 ». */
object FeeEventDateFormat {
    fun label(createdAtEpochMs: Long): String {
        val z = Instant.ofEpochMilli(createdAtEpochMs).atZone(AppClock.zone)
        return "%02d/%02d/%02d".format(z.dayOfMonth, z.monthValue, z.year % 100)
    }
}
