package be.monmoisclair.app.data.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackupChecksumTest {

    @Test
    fun sha256_isDeterministicAndHexLowercase() {
        val a = BackupChecksum.sha256Hex("""{"a":1}""")
        val b = BackupChecksum.sha256Hex("""{"a":1}""")
        assertThat(a).isEqualTo(b)
        assertThat(a).matches("[0-9a-f]{64}")
    }

    @Test
    fun sha256_changesWhenPayloadChanges() {
        val a = BackupChecksum.sha256Hex("""{"amount":47000}""")
        val b = BackupChecksum.sha256Hex("""{"amount":4700}""")
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun matches_isCaseInsensitive() {
        val hex = BackupChecksum.sha256Hex("hello")
        assertThat(BackupChecksum.matches(hex.uppercase(), "hello")).isTrue()
        assertThat(BackupChecksum.matches(hex, "hello!")).isFalse()
    }
}
