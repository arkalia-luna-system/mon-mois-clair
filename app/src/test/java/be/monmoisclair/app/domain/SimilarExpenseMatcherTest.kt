package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SimilarExpenseMatcherTest {

    private fun cand(
        label: String,
        cents: Long = 5_000L,
        day: Int = 5,
        hint: String = ""
    ) = SimilarExpenseMatcher.Candidate(label, cents, day, hint)

    @Test
    fun isSimilar_requiresSameAmountAndDay() {
        val base = cand("Proximus")
        assertThat(
            SimilarExpenseMatcher.isSimilar(base, cand("Proximus"))
        ).isTrue()
        assertThat(
            SimilarExpenseMatcher.isSimilar(base, cand("Proximus", cents = 6_000L))
        ).isFalse()
        assertThat(
            SimilarExpenseMatcher.isSimilar(base, cand("Proximus", day = 6))
        ).isFalse()
    }

    @Test
    fun labelsSimilar_ignoresCaseAndSpaces() {
        assertThat(SimilarExpenseMatcher.labelsSimilar("  Engie ", "engie")).isTrue()
    }

    @Test
    fun labelsSimilar_partialContained() {
        assertThat(SimilarExpenseMatcher.labelsSimilar("Engie gaz", "Engie")).isTrue()
    }

    @Test
    fun isSimilar_matchesViaCauseHint() {
        val newer = cand("Huissier", hint = "")
        val existing = cand("Versement mensuel", hint = "Huissier Dupont")
        assertThat(SimilarExpenseMatcher.isSimilar(newer, existing)).isTrue()
    }

    @Test
    fun confirmDialogBody_namesExistingLine() {
        val body = SimilarExpenseMatcher.confirmDialogBody(
            SimilarExpenseMatcher.Match("Loyer", 12, 80_000L)
        )
        assertThat(body).contains("même facture")
        assertThat(body).contains("Loyer")
        assertThat(body).contains("jour 12")
        assertThat(body).contains("quand même")
    }
}
