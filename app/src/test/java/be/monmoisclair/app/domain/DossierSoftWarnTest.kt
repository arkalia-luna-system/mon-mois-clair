package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DossierSoftWarnTest {

    @Test
    fun empty_whenNotAgreement() {
        assertThat(
            DossierSoftWarn.messages(
                isAgreementOrOpening = false,
                causeLabel = "",
                hasPhoto = false,
                iban = "",
                creditorEmail = ""
            )
        ).isEmpty()
    }

    @Test
    fun allThree_whenAgreementMissingEverything() {
        val msgs = DossierSoftWarn.messages(
            isAgreementOrOpening = true,
            causeLabel = "  ",
            hasPhoto = false,
            iban = "",
            creditorEmail = ""
        )
        // causeLabel "  " isNotBlank in Kotlin... wait, "  ".isBlank() is true
        assertThat(msgs).contains(DossierSoftWarn.MISSING_CAUSE)
        assertThat(msgs).contains(DossierSoftWarn.MISSING_PHOTO)
        assertThat(msgs).contains(DossierSoftWarn.MISSING_CONTACT)
        assertThat(msgs).hasSize(3)
    }

    @Test
    fun none_whenComplete() {
        assertThat(
            DossierSoftWarn.messages(
                isAgreementOrOpening = true,
                causeLabel = "Hôpital",
                hasPhoto = true,
                iban = "BE00",
                creditorEmail = ""
            )
        ).isEmpty()
    }

    @Test
    fun contactOk_withEmailOnly() {
        val msgs = DossierSoftWarn.messages(
            isAgreementOrOpening = true,
            causeLabel = "Hôpital",
            hasPhoto = true,
            iban = "",
            creditorEmail = "a@b.be"
        )
        assertThat(msgs).isEmpty()
    }
}
