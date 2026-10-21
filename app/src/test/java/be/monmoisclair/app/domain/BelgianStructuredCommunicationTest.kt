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
        val compact = BelgianStructuredCommunication.ibanForClipboard("be68 5390 0754 7034")
        assertThat(compact).isEqualTo("BE68539007547034")
        assertThat(BelgianStructuredCommunication.ibanLooksCopyable(compact)).isTrue()
        assertThat(BelgianStructuredCommunication.ibanLooksCopyable("123")).isFalse()
    }

    @Test
    fun empty_yieldsNoClipboard() {
        val copy = BelgianStructuredCommunication.prepareCopy("   ")
        assertThat(copy.clipboardText).isEmpty()
        assertThat(copy.userMessage).contains("Aucune communication")
    }
}
