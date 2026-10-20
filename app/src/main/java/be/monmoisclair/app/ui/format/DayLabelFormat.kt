package be.monmoisclair.app.ui.format

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object DayLabelFormat {
    private val frBe = Locale.forLanguageTag("fr-BE")
    private val monthTitleFmt = DateTimeFormatter.ofPattern("MMMM yyyy", frBe)
    private val monthOnlyFmt = DateTimeFormatter.ofPattern("MMM", frBe)

    fun dayOfMonthLabel(day: Int): String = "Le $day"

    fun monthTitle(ym: YearMonth): String {
        val raw = ym.atDay(1).format(monthTitleFmt)
        return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }
    }

    /** Chip Menu : mois seul (l’année est dans l’en-tête de groupe). */
    fun monthChipShort(ym: YearMonth): String =
        ym.atDay(1).format(monthOnlyFmt)
            .replace(".", "")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }

    /** Chip Menu : court ; année seulement si ≠ année courante. */
    fun monthChip(ym: YearMonth, current: YearMonth): String {
        val short = monthChipShort(ym)
        return if (ym.year == current.year) short else "$short ${ym.year}"
    }
}
