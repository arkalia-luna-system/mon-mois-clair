package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CreditorMailComposerTest {

    @Test
    fun build_containsWhoWhatAmountAndReference() {
        val draft = CreditorMailComposer.build(
            email = "etude@example.be",
            who = "Étude Leroy",
            what = "Hôpital 2025",
            monthAmountCents = 5_000L,
            paymentNote = "+++000/0000/00097+++"
        )
        assertThat(draft.to).isEqualTo("etude@example.be")
        assertThat(draft.subject).contains("00097")
        assertThat(draft.body).contains("Étude Leroy")
        assertThat(draft.body).contains("Hôpital 2025")
        assertThat(draft.body).contains("50 €")
        assertThat(draft.body).doesNotContain("receipts/")
    }
}
