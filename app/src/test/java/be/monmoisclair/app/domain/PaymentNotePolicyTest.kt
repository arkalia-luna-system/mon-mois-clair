package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PaymentNotePolicyTest {

    @Test
    fun carry_forward_dossier_and_autodebit_only() {
        assertThat(PaymentNotePolicy.shouldCarryForward(60_000L, false)).isTrue()
        assertThat(PaymentNotePolicy.shouldCarryForward(null, true)).isTrue()
        assertThat(PaymentNotePolicy.shouldCarryForward(null, false)).isFalse()
    }

    @Test
    fun note_for_new_instance_clears_ordinary() {
        val note = "+++090/9337/55493+++"
        assertThat(
            PaymentNotePolicy.noteForNewInstance(note, openingRemainingCents = null, isAutoDebit = false)
        ).isEmpty()
        assertThat(
            PaymentNotePolicy.noteForNewInstance(note, openingRemainingCents = 10_000L, isAutoDebit = false)
        ).isEqualTo(note)
        assertThat(
            PaymentNotePolicy.noteForNewInstance(note, openingRemainingCents = null, isAutoDebit = true)
        ).isEqualTo(note)
    }
}
