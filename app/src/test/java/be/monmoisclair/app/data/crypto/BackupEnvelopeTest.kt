package be.monmoisclair.app.data.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupEnvelopeTest {

    @Test
    fun seal_open_roundTrip_binary() {
        val secret = "12345678".toCharArray()
        val plain = "bonjour carnet".toByteArray(Charsets.UTF_8)
        val sealed = BackupEnvelope.seal(plain, secret)
        assertThat(BackupEnvelope.looksLikeBinary(sealed)).isTrue()
        assertThat(BackupSecurityArchiveLooksLikeZip(sealed)).isFalse()
        val opened = BackupEnvelope.open(sealed, secret)
        assertThat(opened.toString(Charsets.UTF_8)).isEqualTo("bonjour carnet")
    }

    @Test
    fun seal_open_roundTrip_utf8() {
        val secret = "87654321".toCharArray()
        val sealed = BackupEnvelope.sealToUtf8("""{"ok":true}""", secret)
        assertThat(BackupEnvelope.looksLikeText(sealed)).isTrue()
        assertThat(BackupEnvelope.openUtf8(sealed, secret)).isEqualTo("""{"ok":true}""")
    }

    @Test
    fun wrong_secret_fails() {
        val sealed = BackupEnvelope.seal("secret".toByteArray(), "11111111".toCharArray())
        assertThrows(Exception::class.java) {
            BackupEnvelope.open(sealed, "22222222".toCharArray())
        }
    }

    @Test
    fun whatsapp_without_code_cannot_read() {
        val sealed = BackupEnvelope.seal(
            """{"iban":"BE00"}""".toByteArray(),
            "99887766".toCharArray()
        )
        // Sans déchiffrement : pas de JSON lisible.
        val asText = runCatching { sealed.toString(Charsets.UTF_8) }.getOrNull().orEmpty()
        assertThat(asText.contains("iban")).isFalse()
        assertThat(asText.contains("BE00")).isFalse()
    }

    @Test
    fun plaintext_json_is_not_mistaken_for_sealed() {
        val plain = """{"schemaVersion":8,"payload":{}}"""
        assertThat(BackupEnvelope.looksLikeText(plain)).isFalse()
        assertThat(BackupEnvelope.looksLikeBinary(plain.toByteArray())).isFalse()
    }

    @Test
    fun sealed_utf8_always_starts_with_mmc_prefix() {
        val sealed = BackupEnvelope.sealToUtf8("{}", "11223344".toCharArray())
        assertThat(sealed).startsWith(BackupEnvelope.TEXT_PREFIX)
        assertThat(sealed).doesNotContain("schemaVersion")
    }

    private fun BackupSecurityArchiveLooksLikeZip(bytes: ByteArray): Boolean =
        bytes.size >= 4 &&
            bytes[0] == 0x50.toByte() &&
            bytes[1] == 0x4B.toByte()
}
