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
class DbPassphraseVaultTest {

    private lateinit var context: Context
    private lateinit var vault: DbPassphraseVault

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        File(context.filesDir, DbPassphraseVault.DIR).deleteRecursively()
        vault = DbPassphraseVault(context)
    }

    @Test
    fun unwrapPassphraseChars_isStableHex64() {
        val a = vault.unwrapPassphraseChars()
        val b = vault.unwrapPassphraseChars()
        try {
            assertThat(a).hasLength(64)
            assertThat(String(a).all { it in "0123456789abcdef" }).isTrue()
            assertThat(String(b)).isEqualTo(String(a))
        } finally {
            a.fill('\u0000')
            b.fill('\u0000')
        }
    }

    @Test
    fun encryptedMarker_roundTrip() {
        assertThat(vault.isDbEncryptedMarkerPresent()).isFalse()
        vault.markDbEncrypted()
        assertThat(vault.isDbEncryptedMarkerPresent()).isTrue()
    }
}
