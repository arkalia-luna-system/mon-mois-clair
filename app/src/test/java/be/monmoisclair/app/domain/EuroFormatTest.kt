package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EuroFormatTest {

    @Test
    fun formatCents_thousandsWithThinSpace() {
        assertThat(EuroFormat.formatCents(123_400L)).isEqualTo("1\u202F234 €")
        assertThat(EuroFormat.formatCents(1_234_567L)).isEqualTo("12\u202F345,67 €")
        assertThat(EuroFormat.formatCents(5_000L)).isEqualTo("50 €")
    }
}
