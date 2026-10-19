package be.monmoisclair.app.data.db

import be.monmoisclair.app.domain.model.BackupHealth
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun expenseStatus_roundTrip() {
        ExpenseStatus.entries.forEach { status ->
            val stored = converters.fromExpenseStatus(status)
            assertThat(converters.toExpenseStatus(stored)).isEqualTo(status)
        }
    }

    @Test
    fun backupHealth_roundTrip() {
        BackupHealth.entries.forEach { health ->
            val stored = converters.fromBackupHealth(health)
            assertThat(converters.toBackupHealth(stored)).isEqualTo(health)
        }
    }

    @Test
    fun expenseStatus_rejectsUnknownString() {
        assertThrows(IllegalArgumentException::class.java) {
            converters.toExpenseStatus("paid")
        }
    }
}
