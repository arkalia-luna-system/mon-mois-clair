package be.monmoisclair.app.data.crypto

import android.content.Context
import android.os.UserManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ShareSecretVaultTest {

    private lateinit var context: Context
    private lateinit var vault: ShareSecretVault

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val um = context.getSystemService(UserManager::class.java)!!
        shadowOf(um).setUserUnlocked(true)
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
    fun ensureProvisioned_refusesWhenUserLocked() {
        val um = context.getSystemService(UserManager::class.java)!!
        shadowOf(um).setUserUnlocked(false)
        val thrown = runCatching { vault.peekDisplayCode() }.exceptionOrNull()
        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        assertThat(thrown?.message).contains("CE indisponible")
    }

    @Test
    fun peekDisplayCode_recoversFromCorruptedWrappedFile() {
        val wrapped = File(context.filesDir, "${DbPassphraseVault.DIR}/${ShareSecretVault.FILE_NAME}")
        wrapped.parentFile!!.mkdirs()
        wrapped.writeText("pas-du-base64")

        val code = vault.peekDisplayCode()

        assertThat(code).hasLength(8)
        assertThat(code.all { it.isDigit() }).isTrue()
        assertThat(vault.peekDisplayCode()).isEqualTo(code)
    }

    @Test
    fun generateNumericCode_respectsDigitCount() {
        val code = ShareSecretVault.generateNumericCode(8)
        assertThat(code).hasLength(8)
        assertThat(code.all { it.isDigit() }).isTrue()
    }
}
