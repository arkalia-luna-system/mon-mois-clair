package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EpcQrParserTest {

    private val sampleWithPurpose = """
        BCD
        002
        1
        SCT
        GEBABEBB
        VIVAQUA
        BE68012345678912
        EUR74.50
        CHAR
        +++090/9337/55493+++
        Facture eau
    """.trimIndent()

    /** QR « maigre » : pas de ligne Purpose — remittance glisse à l’index 8. */
    private val sampleWithoutPurpose = """
        BCD
        002
        1
        SCT
        GEBABEBB
        ENGIE
        BE68539007547034
        EUR120.00
        +++123/4567/89002+++
        Facture énergie
    """.trimIndent()

    private val sampleEmptyPurposeLine = """
        BCD
        002
        1
        SCT
        GEBABEBB
        PROXIMUS
        BE68539007547034
        EUR45.00

        +++090/9337/55493+++
    """.trimIndent()

    private val sampleFreeRemittanceNoPurpose = """
        BCD
        002
        1
        SCT
        GEBABEBB
        LOYER
        BE68539007547034
        EUR800.00
        Loyer octobre 2026
    """.trimIndent()

    @Test
    fun parses_belgian_epc_qr() {
        val p = EpcQrParser.parse(sampleWithPurpose)!!
        assertThat(p.beneficiaryName).isEqualTo("VIVAQUA")
        assertThat(p.iban).isEqualTo("BE68012345678912")
        assertThat(p.amountCents).isEqualTo(7450L)
        assertThat(p.remittance).isEqualTo("+++090/9337/55493+++")
    }

    @Test
    fun parses_without_purpose_line() {
        val p = EpcQrParser.parse(sampleWithoutPurpose)!!
        assertThat(p.beneficiaryName).isEqualTo("ENGIE")
        assertThat(p.amountCents).isEqualTo(12_000L)
        assertThat(p.remittance).isEqualTo("+++123/4567/89002+++")
    }

    @Test
    fun parses_empty_purpose_line() {
        val p = EpcQrParser.parse(sampleEmptyPurposeLine)!!
        assertThat(p.remittance).isEqualTo("+++090/9337/55493+++")
    }

    @Test
    fun parses_free_remittance_without_purpose() {
        val p = EpcQrParser.parse(sampleFreeRemittanceNoPurpose)!!
        assertThat(p.remittance).isEqualTo("Loyer octobre 2026")
    }

    @Test
    fun rejects_non_bcd() {
        assertThat(EpcQrParser.parse("NOPE")).isNull()
    }

    @Test
    fun amount_eur_comma() {
        assertThat(EpcQrParser.parseAmountCents("EUR1,50")).isEqualTo(150L)
    }

    @Test
    fun amount_empty_is_null() {
        assertThat(EpcQrParser.parseAmountCents("")).isNull()
    }

    @Test
    fun resolveRemittance_prefers_structured_over_purpose() {
        assertThat(
            EpcQrParser.resolveRemittance(
                listOf("CHAR", "+++090/9337/55493+++", "info")
            )
        ).isEqualTo("+++090/9337/55493+++")
    }
}
