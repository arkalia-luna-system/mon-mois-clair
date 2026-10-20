package be.monmoisclair.app.ui.settings

import be.monmoisclair.app.domain.usecase.OffMonthPeriodic
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OffMonthPeriodicFormatTest {

    @Test
    fun amountLine_annual() {
        val item = OffMonthPeriodic(
            templateId = "tpl-ass",
            label = "Assurance auto",
            amountCents = 45_000,
            dayOfMonth = 12,
            intervalMonths = 12,
            nextDueYearMonth = "2027-03",
            nextDueLabel = "mars 2027"
        )
        assertThat(OffMonthPeriodicFormat.amountLine(item))
            .contains("le 12")
        assertThat(OffMonthPeriodicFormat.amountLine(item))
            .contains("1× par an")
    }

    @Test
    fun amountLine_quarterly() {
        val item = OffMonthPeriodic(
            templateId = "tpl-eau",
            label = "Eau",
            amountCents = 8_050,
            dayOfMonth = 5,
            intervalMonths = 3,
            nextDueYearMonth = "2027-01",
            nextDueLabel = "janvier 2027"
        )
        assertThat(OffMonthPeriodicFormat.amountLine(item))
            .contains("le 5")
        assertThat(OffMonthPeriodicFormat.amountLine(item))
            .contains("tous les 3 mois")
    }
}
