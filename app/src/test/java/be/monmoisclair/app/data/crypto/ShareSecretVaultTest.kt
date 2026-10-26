package be.monmoisclair.app.data.crypto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ShareSecretVaultTest {

    private lateinit var context: Context
    private lateinit var vault: ShareSecretVault

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        File(context.filesDir, DbPassphraseVault.DIR).deleteRecursively()
        vault = ShareSecretVault(context)
    }

    @Test
    fun getOrCreate_isEightDigits_andStable() {
        val first = vault.peekDisplayCode()
        val second = vault.peekDisplayCode()
        assertThat(first).hasLength(8)
        assertThat(first.all { it.isDigit() }).isTrue()
        assertThat(second).isEqualTo(first)
        val chars = vault.getOrCreateSecretChars()
        try {
            assertThat(String(chars)).isEqualTo(first)
        } finally {
            chars.fill('\u0000')
        }
    }

    @Test
    fun generateNumericCode_respectsDigitCount() {
        val code = ShareSecretVault.generateNumericCode(8)
        assertThat(code).hasLength(8)
        assertThat(code.all { it.isDigit() }).isTrue()
    }
}
