package be.monmoisclair.app.data.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import javax.crypto.spec.SecretKeySpec

class AesGcmBoxTest {

    @Test
    fun encrypt_decrypt_roundTrip() {
        val key = SecretKeySpec(AesGcmBox.randomBytes(32), "AES")
        val plain = "données famille".toByteArray(Charsets.UTF_8)
        val sealed = AesGcmBox.encrypt(key, plain)
        assertThat(sealed.size).isGreaterThan(plain.size)
        assertThat(sealed.size).isAtLeast(AesGcmBox.IV_BYTES + plain.size + 16)
        assertThat(AesGcmBox.decrypt(key, sealed).toString(Charsets.UTF_8))
            .isEqualTo("données famille")
    }

    @Test
    fun encrypt_usesDistinctIvEachCall() {
        val key = SecretKeySpec(AesGcmBox.randomBytes(32), "AES")
        val plain = byteArrayOf(9, 8, 7)
        val a = AesGcmBox.encrypt(key, plain)
        val b = AesGcmBox.encrypt(key, plain)
        assertThat(a.copyOfRange(0, AesGcmBox.IV_BYTES))
            .isNotEqualTo(b.copyOfRange(0, AesGcmBox.IV_BYTES))
        assertThat(AesGcmBox.decrypt(key, a)).isEqualTo(plain)
        assertThat(AesGcmBox.decrypt(key, b)).isEqualTo(plain)
    }

    @Test
    fun pbkdf2_wrong_password_fails() {
        val salt = AesGcmBox.randomBytes(AesGcmBox.SALT_BYTES)
        val keyOk = AesGcmBox.keyFromSecret("11111111".toCharArray(), salt)
        val keyBad = AesGcmBox.keyFromSecret("00000000".toCharArray(), salt)
        val sealed = AesGcmBox.encrypt(keyOk, byteArrayOf(1, 2, 3))
        assertThrows(Exception::class.java) {
            AesGcmBox.decrypt(keyBad, sealed)
        }
    }
}
