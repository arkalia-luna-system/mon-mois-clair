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
        assertThat(MoneyFormat.parseToCents("12.34.56")).isNull()
        // E07 : pas de troncature silencieuse
        assertThat(MoneyFormat.parseToCents("1.999")).isNull()
        assertThat(MoneyFormat.parseToCents("12,999")).isNull()
    }

    @Test
    fun formatSignedCents_expenseAndIncome() {
        assertThat(MoneyFormat.formatSignedCents(47_000L, isExpense = true)).contains("470")
        // Signe moins typographique U+2212 (pas le tiret ASCII)
        assertThat(MoneyFormat.formatSignedCents(47_000L, isExpense = true)).startsWith("\u2212")
        assertThat(MoneyFormat.formatSignedCents(100_000L, isExpense = false)).startsWith("+")
    }

    @Test
    fun formatCents_belgianPremium() {
        assertThat(MoneyFormat.formatCents(47_000L)).isEqualTo("470 €")
        assertThat(MoneyFormat.formatCents(47_050L)).isEqualTo("470,50 €")
        assertThat(MoneyFormat.formatCents(123_456L)).isEqualTo("1\u202F234,56 €")
        assertThat(MoneyFormat.formatCents(-1_500L)).startsWith("\u2212")
    }
}
