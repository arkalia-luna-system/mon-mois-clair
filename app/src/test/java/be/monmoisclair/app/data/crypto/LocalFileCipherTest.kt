package be.monmoisclair.app.data.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class LocalFileCipherTest {

    @Test
    fun seal_open_roundTrip_and_plain_passthrough() {
        val plain = "%PDF-1.4 hello".toByteArray()
        val sealed = LocalFileCipher.seal(plain)
        assertThat(LocalFileCipher.looksEncrypted(sealed)).isTrue()
        assertThat(LocalFileCipher.open(sealed)).isEqualTo(plain)
        // Grace : fichier clair pré-6.81
        assertThat(LocalFileCipher.open(plain)).isEqualTo(plain)
    }

    @Test
    fun migrateDirectory_encrypts_webp() {
        val dir = File.createTempFile("receipts", "dir").also {
            it.delete()
            it.mkdirs()
        }
        try {
            val f = File(dir, "exp.webp")
            f.writeBytes(byteArrayOf(1, 2, 3, 4, 5))
            val n = LocalFileCipher.migrateDirectory(dir)
            assertThat(n).isEqualTo(1)
            assertThat(LocalFileCipher.looksEncrypted(f.readBytes())).isTrue()
            assertThat(LocalFileCipher.readDecrypted(f)).isEqualTo(byteArrayOf(1, 2, 3, 4, 5))
            // 2ᵉ passe : déjà chiffré
            assertThat(LocalFileCipher.migrateDirectory(dir)).isEqualTo(0)
        } finally {
            dir.deleteRecursively()
        }
    }
}
