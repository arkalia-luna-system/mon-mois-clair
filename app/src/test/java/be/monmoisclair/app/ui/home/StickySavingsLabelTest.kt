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
}
