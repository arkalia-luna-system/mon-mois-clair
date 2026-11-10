package be.monmoisclair.app.data.reminder

import be.monmoisclair.app.domain.model.ReminderKind
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PaymentNotificationCancelTest {

    @Test
    fun ids_coverAllKinds() {
        val ids = PaymentNotificationCancel.notificationIdsForExpense("exp-1")
        assertThat(ids).hasSize(ReminderKind.entries.size)
        assertThat(ids.distinct()).hasSize(ids.size)
    }
}
