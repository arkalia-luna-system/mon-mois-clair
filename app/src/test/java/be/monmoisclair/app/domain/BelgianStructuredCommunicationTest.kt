package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BelgianStructuredCommunicationTest {

    @Test
    fun exampleFromPlan_isValid_andCopyYieldsTwelveDigits() {
        val display = BelgianStructuredCommunication.EXAMPLE_VALID_DISPLAY
        assertThat(BelgianStructuredCommunication.isValidMod97("000000000097")).isTrue()
        val copy = BelgianStructuredCommunication.prepareCopy(display)
        assertThat(copy.clipboardText).isEqualTo("000000000097")
        assertThat(copy.userMessage).isEqualTo(BelgianStructuredCommunication.COPIED_OK)
    }

    @Test
    fun invalidCheckDigits_copiesAsIs_withVerifyMessage() {
        val bad = "+++000/0000/00098+++"
        assertThat(BelgianStructuredCommunication.isValidMod97("000000000098")).isFalse()
        val copy = BelgianStructuredCommunication.prepareCopy(bad)
        assertThat(copy.clipboardText).isEqualTo(bad)
        assertThat(copy.userMessage).isEqualTo(BelgianStructuredCommunication.VERIFY_ON_MAIL)
    }

    @Test
    fun freeNote_copiedUnchanged() {
        val copy = BelgianStructuredCommunication.prepareCopy("loyer")
        assertThat(copy.clipboardText).isEqualTo("loyer")
        assertThat(copy.userMessage).isEqualTo(BelgianStructuredCommunication.COPIED_OK)
    }

    @Test
    fun twelveDigitsAlone_valid_goesToClipboard() {
        // 1234567890 % 97 = 2 → 123456789002
        val digits = "123456789002"
        assertThat(BelgianStructuredCommunication.isValidMod97(digits)).isTrue()
        val copy = BelgianStructuredCommunication.prepareCopy(digits)
        assertThat(copy.clipboardText).isEqualTo(digits)
    }

    @Test
    fun freeNoteWithDigits_isNotStripped() {
        val copy = BelgianStructuredCommunication.prepareCopy("loyer 000000000097")
        assertThat(copy.clipboardText).isEqualTo("loyer 000000000097")
        assertThat(copy.userMessage).isEqualTo(BelgianStructuredCommunication.COPIED_OK)
    }

    @Test
    fun iban_stripsSpacesAndKeepsLetters() {
        val compact = BelgianStructuredCommunication.ibanForClipboard("IBAN be68 5390 0754 7034")
        assertThat(compact).isEqualTo("BE68539007547034")
        assertThat(BelgianStructuredCommunication.ibanLooksCopyable(compact)).isTrue()
        assertThat(BelgianStructuredCommunication.ibanLooksCopyable("123")).isFalse()
        assertThat(BelgianStructuredCommunication.ibanLooksCopyable("BEAB539007547034")).isFalse()
        assertThat(BelgianStructuredCommunication.ibanChecksumOk(compact)).isTrue()
        assertThat(BelgianStructuredCommunication.ibanChecksumOk("BE68539007547035")).isFalse()
        assertThat(BelgianStructuredCommunication.ibanChecksumOk("GB82WEST12345698765432")).isTrue()
    }

    @Test
    fun empty_yieldsNoClipboard() {
        val copy = BelgianStructuredCommunication.prepareCopy("   ")
        assertThat(copy.clipboardText).isEmpty()
        assertThat(copy.userMessage).contains("Aucune communication")
    }

    @Test
    fun findInOcrText_findsPlusForm_amidNoise_ignoresAmount() {
        val ocr = """
            FACTURE ÉNERGIE
            Montant : 45,67 EUR
            +++000/0000/00097+++
            Merci
        """.trimIndent()
        assertThat(BelgianStructuredCommunication.findInOcrText(ocr))
            .isEqualTo("+++000/0000/00097+++")
    }

    @Test
    fun findInOcrText_twelveDigitsAlone_whenValid() {
        val ocr = "Réf bancaire 000000000097 à coller"
        assertThat(BelgianStructuredCommunication.findInOcrText(ocr))
            .isEqualTo("+++000/0000/00097+++")
    }

    @Test
    fun findInOcrText_doesNotGlueSpacedDigits() {
        // 123456789002 est valide, mais séparateurs / sans forme +++ → refusé.
        assertThat(
            BelgianStructuredCommunication.findInOcrText("montant 1234 5678 9002 EUR")
        ).isNull()
    }

    @Test
    fun findInOcrText_twelveDigitsWithoutContext_ignored() {
        assertThat(BelgianStructuredCommunication.findInOcrText("000000000097")).isNull()
        assertThat(
            BelgianStructuredCommunication.findInOcrText("Total 1.234,56 puis 000000000097")
        ).isNull()
    }

    @Test
    fun findInOcrText_ignoresInvalidTwelve_andAmountNoise() {
        assertThat(
            BelgianStructuredCommunication.findInOcrText("À payer 45,67 EUR — 000000000098")
        ).isNull()
        assertThat(BelgianStructuredCommunication.findInOcrText("pas de chiffres")).isNull()
    }

    @Test
    fun formatDisplay_rejectsInvalid() {
        assertThat(BelgianStructuredCommunication.formatDisplay("000000000098")).isNull()
        assertThat(BelgianStructuredCommunication.formatDisplay("000000000097"))
            .isEqualTo("+++000/0000/00097+++")
    }
}
