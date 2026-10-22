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

    @Test
    fun without_unpaid_only_when_still_to_pay() {
        assertThat(stickyWithoutUnpaidLabel(45_000L, 0L)).isNull()
        assertThat(
            stickyWithoutUnpaidLabel(45_000L, 30_000L) { cents -> "${cents / 100} €" }
        ).isEqualTo("Après les FAIT 750 €")
    }

    @Test
    fun future_preview_uses_prevu_hides_fait_line() {
        assertThat(
            stickyStillToPayLabel(30_000L, hasExpenses = true, futurePreview = true) { "300 €" }
        ).isEqualTo("Prévu 300 €")
        assertThat(
            stickyStillToPayLabel(0L, hasExpenses = true, futurePreview = true)
        ).isNull()
        assertThat(
            stickyWithoutUnpaidLabel(45_000L, 30_000L, futurePreview = true)
        ).isNull()
    }
}
