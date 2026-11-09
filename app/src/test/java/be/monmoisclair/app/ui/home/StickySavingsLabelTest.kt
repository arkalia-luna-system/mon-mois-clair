package be.monmoisclair.app.ui.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StickySavingsLabelTest {

    @Test
    fun inactive_showsEntry() {
        assertThat(stickySavingsLabel(0L)).isEqualTo("Mettre de côté")
    }

    @Test
    fun active_showsAmount() {
        assertThat(stickySavingsLabel(50_00L) { "${it / 100} €" })
            .isEqualTo("Mettre de côté 50 €")
    }

    @Test
    fun futurePreview_hides() {
        assertThat(stickySavingsLabel(50_00L, futurePreview = true)).isNull()
    }

    @Test
    fun chip_shows_amount_or_cote() {
        assertThat(stickySavingsChipLabel(0L)).isEqualTo("Côté")
        assertThat(stickySavingsChipLabel(50_00L) { "${it / 100} €" }).isEqualTo("50 €")
        assertThat(stickySavingsChipLabel(50_00L, futurePreview = true)).isNull()
    }

    @Test
    fun sheet_inactive_prompt() {
        assertThat(savingsSheetStatusLine(0L)).contains("autre compte")
    }

    @Test
    fun sheet_active_matches_visible() {
        assertThat(
            savingsSheetStatusLine(50_00L) { c -> "${c / 100} €" }
        ).isEqualTo("Noté 50 € / mois · déjà retiré du reste")
    }
}
