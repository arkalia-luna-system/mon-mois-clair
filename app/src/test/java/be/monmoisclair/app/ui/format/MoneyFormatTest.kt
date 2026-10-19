package be.monmoisclair.app.ui.format

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MoneyFormatTest {

    @Test
    fun parseToCents_acceptsCommaAndDot() {
        assertThat(MoneyFormat.parseToCents("470")).isEqualTo(47_000L)
        assertThat(MoneyFormat.parseToCents("470,50")).isEqualTo(47_050L)
        assertThat(MoneyFormat.parseToCents("470.5")).isEqualTo(47_050L)
    }

    @Test
    fun parseToCents_rejectsInvalid() {
        assertThat(MoneyFormat.parseToCents("")).isNull()
        assertThat(MoneyFormat.parseToCents("-10")).isNull()
        assertThat(MoneyFormat.parseToCents("abc")).isNull()
    }

    @Test
    fun formatSignedCents_expenseAndIncome() {
        assertThat(MoneyFormat.formatSignedCents(47_000L, isExpense = true)).contains("470")
        assertThat(MoneyFormat.formatSignedCents(47_000L, isExpense = true)).startsWith("-")
        assertThat(MoneyFormat.formatSignedCents(100_000L, isExpense = false)).startsWith("+")
    }
}
