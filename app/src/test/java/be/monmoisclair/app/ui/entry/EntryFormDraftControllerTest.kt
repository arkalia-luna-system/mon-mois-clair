package be.monmoisclair.app.ui.entry

import be.monmoisclair.app.domain.EpcPayment
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EntryFormDraftControllerTest {

    private fun blank() = EntryFormDraftController.PaymentFields(
        label = "",
        amountText = "",
        paymentNote = "",
        iban = "",
        copyMessage = null,
        openingRemainingText = "",
        dossierExpanded = false
    )

    @Test
    fun applyEpc_fillsBeneficiaryAmountAndNote() {
        val payment = EpcPayment(
            beneficiaryName = "Loyer",
            amountCents = 85_000L,
            remittance = "+++123/4567/89012+++",
            iban = "BE68539007547034"
        )
        val result = EntryFormDraftController.applyEpc(blank(), payment)
        assertThat(result.forceExpense).isTrue()
        assertThat(result.fields.label).isEqualTo("Loyer")
        assertThat(result.fields.paymentNote).contains("123")
        assertThat(result.fields.iban).isEqualTo("BE68539007547034")
        assertThat(result.fields.amountText).isNotEmpty()
    }

    @Test
    fun applyEpcRawOrNull_rejectsGarbage() {
        assertThat(EntryFormDraftController.applyEpcRawOrNull(blank(), "not-a-qr"))
            .isNull()
    }

    @Test
    fun ocrFound_setsNoteAndMessage() {
        val updated = EntryFormDraftController.ocrFound(blank(), "+++111/2222/33333+++")
        assertThat(updated.paymentNote).startsWith("+++")
        assertThat(updated.copyMessage).isNotNull()
    }

    @Test
    fun resolveOcr_blank_isNotFound() {
        val outcome = EntryFormDraftController.resolveOcr(blank(), "  ")
        assertThat(outcome).isInstanceOf(EntryFormDraftController.OcrOutcome.NotFound::class.java)
    }

    @Test
    fun resolveOcr_value_isApplied() {
        val outcome = EntryFormDraftController.resolveOcr(blank(), "+++111/2222/33333+++")
        assertThat(outcome).isInstanceOf(EntryFormDraftController.OcrOutcome.Applied::class.java)
        val applied = outcome as EntryFormDraftController.OcrOutcome.Applied
        assertThat(applied.fields.paymentNote).startsWith("+++")
    }

    @Test
    fun collapseConversionDossier_wipesOnCollapse() {
        val (expanded, cause, opening) = EntryFormDraftController.collapseConversionDossier(
            expanded = false,
            causeLabel = "retard",
            openingRemainingText = "120"
        )
        assertThat(expanded).isFalse()
        assertThat(cause).isEmpty()
        assertThat(opening).isEmpty()
    }

    @Test
    fun paymentPatch_mirrorsEpcFields() {
        val payment = EpcPayment(
            beneficiaryName = "Gaz",
            amountCents = 40_000L,
            remittance = "+++999/8888/77777+++",
            iban = "BE68539007547034"
        )
        val patch = EntryFormDraftController.paymentPatch(
            EntryFormDraftController.applyEpc(blank(), payment)
        )
        assertThat(patch.label).isEqualTo("Gaz")
        assertThat(patch.iban).isEqualTo("BE68539007547034")
    }
}
