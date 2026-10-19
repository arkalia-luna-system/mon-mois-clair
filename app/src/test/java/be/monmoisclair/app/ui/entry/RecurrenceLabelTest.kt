package be.monmoisclair.app.ui.entry

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecurrenceLabelTest {

    @Test
    fun forIntervalMonths_knownCadences() {
        assertThat(RecurrenceLabel.forIntervalMonths(1)).isEqualTo("Revient tous les mois")
        assertThat(RecurrenceLabel.forIntervalMonths(3)).isEqualTo("Revient tous les 3 mois")
        assertThat(RecurrenceLabel.forIntervalMonths(12)).isEqualTo("Revient 1× par an")
        assertThat(RecurrenceLabel.forIntervalMonths(6)).isEqualTo("Revient tous les 6 mois")
    }

    @Test
    fun oneOff_and_chips() {
        assertThat(RecurrenceLabel.oneOff()).contains("Une seule fois")
        assertThat(RecurrenceLabel.chipOnce()).isEqualTo("Une seule fois")
        assertThat(RecurrenceLabel.chipMonthly()).isEqualTo("Tous les mois")
        assertThat(RecurrenceLabel.chipEvery3Months()).isEqualTo("Tous les 3 mois")
        assertThat(RecurrenceLabel.chipYearly()).isEqualTo("1× par an")
    }
}
