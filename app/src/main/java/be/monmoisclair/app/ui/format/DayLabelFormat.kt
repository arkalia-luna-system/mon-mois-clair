package be.monmoisclair.app.ui.format

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object DayLabelFormat {
    // Abrégé (ex. « Oct. 2026 ») : évite le wrap / ellipsis entre Préc. et Suiv. sur Ultra.
    private val monthTitleFmt =
        DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("fr-BE"))

    fun dayOfMonthLabel(day: Int): String = "Le $day"

    fun monthTitle(ym: YearMonth): String {
        val raw = ym.atDay(1).format(monthTitleFmt)
        return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }
    }
}
