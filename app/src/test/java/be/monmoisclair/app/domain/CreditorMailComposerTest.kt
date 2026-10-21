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

    @Test
    fun build_usesThinSpaceThousands() {
        val draft = CreditorMailComposer.build(
            email = "a@b.be",
            who = "X",
            what = "",
            monthAmountCents = 123_400L,
            paymentNote = ""
        )
        assertThat(draft.body).contains("1\u202F234 €")
    }

    @Test
    fun mailtoUri_putsSubjectAndBodyInQuery_notExtras() {
        val draft = CreditorMailComposer.Draft(
            to = "etude@example.be",
            subject = "Réf 12",
            body = "Bonjour,\nligne 2"
        )
        val uri = CreditorMailComposer.mailtoUri(draft)
        assertThat(uri).startsWith("mailto:etude@example.be?")
        assertThat(uri).contains("subject=")
        assertThat(uri).contains("body=")
        assertThat(uri).contains("R%C3%A9f") // encodage UTF-8
        assertThat(uri).contains("%0A") // saut de ligne dans le corps
        assertThat(uri).doesNotContain(" ")
    }
}
