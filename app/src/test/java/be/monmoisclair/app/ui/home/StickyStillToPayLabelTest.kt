package be.monmoisclair.app.ui.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StickyStillToPayLabelTest {

    @Test
    fun empty_month_hides_line() {
        assertThat(stickyStillToPayLabel(0L, hasExpenses = false)).isNull()
    }

    @Test
    fun all_paid_shows_tout_est_paye() {
        assertThat(stickyStillToPayLabel(0L, hasExpenses = true)).isEqualTo("Tout est payé")
    }

    @Test
    fun unpaid_shows_a_payer() {
        assertThat(
            stickyStillToPayLabel(30_000L, hasExpenses = true) { "300 €" }
        ).isEqualTo("À payer 300 €")
    }
}
