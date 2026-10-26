package be.monmoisclair.app.ui.entry

import be.monmoisclair.app.domain.EpcPayment
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EntryFormEpcApplyTest {

    @Test
    fun patch_fillsBlankFields_andHintsIbanWhenDossierCollapsed() {
        val payment = EpcPayment(
            beneficiaryName = "Créancier SA",
            iban = "BE68539007547034",
            amountCents = 12_34L,
            remittance = "+++123/4567/89012+++"
        )
        val patch = EntryFormEpcApply.patch(
            payment = payment,
            currentLabel = "",
            currentAmountText = "",
            currentPaymentNote = "",
            currentIban = "",
            currentCopyMessage = null,
            openingRemainingText = "",
            dossierExpanded = false
        )
        assertThat(patch.label).isEqualTo("Créancier SA")
        assertThat(patch.amountText).isEqualTo("12,34")
        assertThat(patch.paymentNote).isEqualTo("+++123/4567/89012+++")
        assertThat(patch.iban).isEqualTo("BE68539007547034")
        assertThat(patch.copyMessage).isEqualTo(EntryFormMessages.EPC_IBAN_HINT)
    }

    @Test
    fun patch_keepsExistingWhenPaymentBlank_andNoHintIfDossierOpen() {
        val payment = EpcPayment(
            beneficiaryName = "",
            iban = "BE68539007547034",
            amountCents = null,
            remittance = ""
        )
        val patch = EntryFormEpcApply.patch(
            payment = payment,
            currentLabel = "Loyer",
            currentAmountText = "900",
            currentPaymentNote = "note",
            currentIban = "OLD",
            currentCopyMessage = "déjà",
            openingRemainingText = "",
            dossierExpanded = true
        )
        assertThat(patch.label).isEqualTo("Loyer")
        assertThat(patch.amountText).isEqualTo("900")
        assertThat(patch.paymentNote).isEqualTo("note")
        assertThat(patch.iban).isEqualTo("BE68539007547034")
        assertThat(patch.copyMessage).isEqualTo("déjà")
    }
}
