package be.monmoisclair.app.data.summary

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MonthSummaryPdfWriterTest {

    /** Largeur = nombre de caractères (indépendant de Paint / PdfDocument). */
    private val byChars: (String) -> Float = { it.length.toFloat() }

    @Test
    fun readableSizes_matchCharterMins() {
        assertThat(MonthSummaryPdfWriter.BODY_PT).isAtLeast(18f)
        assertThat(MonthSummaryPdfWriter.TITLE_PT).isAtLeast(22f)
        assertThat(MonthSummaryPdfWriter.FOOTER_PT).isLessThan(MonthSummaryPdfWriter.BODY_PT)
    }

    @Test
    fun isSectionHeading_rubricsAndMonth_notBullets() {
        assertThat(MonthSummaryPdfWriter.isSectionHeading("Revenus")).isTrue()
        assertThat(MonthSummaryPdfWriter.isSectionHeading("Factures du mois")).isTrue()
        assertThat(MonthSummaryPdfWriter.isSectionHeading("Dossiers (accords)")).isTrue()
        assertThat(MonthSummaryPdfWriter.isSectionHeading("Mis de côté")).isTrue()
        assertThat(MonthSummaryPdfWriter.isSectionHeading("Déficit ce mois : 12,00 €")).isTrue()
        assertThat(MonthSummaryPdfWriter.isSectionHeading("Octobre 2026")).isTrue()
        assertThat(MonthSummaryPdfWriter.isSectionHeading("Août 2026")).isTrue()

        assertThat(MonthSummaryPdfWriter.isSectionHeading("")).isFalse()
        assertThat(MonthSummaryPdfWriter.isSectionHeading("• Loyer — 800,00 €")).isFalse()
        assertThat(MonthSummaryPdfWriter.isSectionHeading("— aucun")).isFalse()
        assertThat(
            MonthSummaryPdfWriter.isSectionHeading("  Versement ce mois : 20,00 € · Reste : 100,00 €")
        ).isFalse()
        assertThat(
            MonthSummaryPdfWriter.isSectionHeading("Reste après factures : −12,00 €")
        ).isFalse()
        assertThat(
            MonthSummaryPdfWriter.isSectionHeading("Mon Mois Clair — résumé")
        ).isFalse()
    }

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
