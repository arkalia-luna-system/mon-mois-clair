package be.monmoisclair.app.data.summary

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MonthSummaryPdfWriterTest {

    /** Largeur = nombre de caractères (indépendant de Paint / Robolectric). */
    private val byChars: (String) -> Float = { it.length.toFloat() }

    @Test
    fun wrapForWidth_keepsShortLine() {
        assertThat(MonthSummaryPdfWriter.wrapForWidth("Court", 40f, byChars))
            .containsExactly("Court")
    }

    @Test
    fun wrapForWidth_splitsLongPhrase() {
        val long = "Revenus Salaire allocations et autres lignes du résumé à montrer"
        val wrapped = MonthSummaryPdfWriter.wrapForWidth(long, 20f, byChars)
        assertThat(wrapped.size).isGreaterThan(1)
        assertThat(wrapped.joinToString(" ")).isEqualTo(long)
        wrapped.forEach { chunk ->
            assertThat(chunk.length).isAtMost(20)
        }
    }

    @Test
    fun wrapForWidth_hardWrapsSingleWord() {
        val word = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val wrapped = MonthSummaryPdfWriter.wrapForWidth(word, 10f, byChars)
        assertThat(wrapped).containsExactly(
            "ABCDEFGHIJ",
            "KLMNOPQRST",
            "UVWXYZ"
        )
    }

    @Test
    fun fileName_isStableShareTarget() {
        assertThat(MonthSummaryPdfWriter.FILE_NAME).endsWith(".pdf")
    }
}
