package be.monmoisclair.app.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EpcQrParserTest {

    private val sample = """
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

    @Test
    fun parses_belgian_epc_qr() {
        val p = EpcQrParser.parse(sample)!!
        assertThat(p.beneficiaryName).isEqualTo("VIVAQUA")
        assertThat(p.iban).isEqualTo("BE68012345678912")
        assertThat(p.amountCents).isEqualTo(7450L)
        assertThat(p.remittance).isEqualTo("+++090/9337/55493+++")
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
}
