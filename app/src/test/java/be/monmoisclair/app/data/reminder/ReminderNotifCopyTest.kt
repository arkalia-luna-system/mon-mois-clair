package be.monmoisclair.app.data.reminder

import be.monmoisclair.app.domain.model.ReminderKind
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReminderNotifCopyTest {

    @Test
    fun amountLabel_masksWhenBiometricOn() {
        assertThat(ReminderNotifCopy.amountLabel(12_34L, hideAmounts = true))
            .isEqualTo(ReminderNotifCopy.MASKED_AMOUNT)
        assertThat(ReminderNotifCopy.amountLabel(12_34L, hideAmounts = false))
            .doesNotContain("masqué")
        assertThat(ReminderNotifCopy.amountLabel(12_34L, hideAmounts = false))
            .contains("12")
    }

    @Test
    fun body_j3_masksAmount_notLabelInBody() {
        val body = ReminderNotifCopy.body(
            kind = ReminderKind.J3,
            amountCents = 50_00L,
            dayOfMonth = 10,
            isAutoDebit = false,
            hideAmounts = true
        )
        assertThat(body).startsWith(ReminderNotifCopy.MASKED_AMOUNT)
        assertThat(body).contains("le 10")
        assertThat(body).doesNotContain("50")
    }

    @Test
    fun title_keepsLabel() {
        assertThat(ReminderNotifCopy.title(ReminderKind.JD, "Loyer"))
            .isEqualTo("Aujourd’hui : Loyer")
        assertThat(ReminderNotifCopy.title(ReminderKind.OVERDUE, "Gaz"))
            .isEqualTo("En retard : Gaz")
    }
}
