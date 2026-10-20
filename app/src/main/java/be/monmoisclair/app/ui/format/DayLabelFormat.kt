package be.monmoisclair.app.ui.format

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object DayLabelFormat {
    private val monthTitleFmt =
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("fr-BE"))

    fun dayOfMonthLabel(day: Int): String = "Le $day"

    fun monthTitle(ym: YearMonth): String {
        val raw = ym.atDay(1).format(monthTitleFmt)
        return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }
    }
}
