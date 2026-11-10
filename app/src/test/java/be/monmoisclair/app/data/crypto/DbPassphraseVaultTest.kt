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
class DbPassphraseVaultTest {

    private lateinit var context: Context
    private lateinit var vault: DbPassphraseVault

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val um = context.getSystemService(UserManager::class.java)!!
        shadowOf(um).setUserUnlocked(true)
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

    @Test
    fun markerPresent_doesNotCreateCryptoDir() {
        val crypto = File(context.filesDir, DbPassphraseVault.DIR)
        assertThat(crypto.exists()).isFalse()
        assertThat(vault.isDbEncryptedMarkerPresent()).isFalse()
        assertThat(crypto.exists()).isFalse()
        assertThat(vault.isMigrationFailedPresent()).isFalse()
        assertThat(crypto.exists()).isFalse()
    }

    @Test
    fun ensureProvisioned_refusesWhenUserLocked() {
        val um = context.getSystemService(UserManager::class.java)!!
        shadowOf(um).setUserUnlocked(false)
        val thrown = runCatching { vault.ensureProvisioned() }.exceptionOrNull()
        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        assertThat(thrown?.message).contains("CE indisponible")
    }

    @Test
    fun markDbEncrypted_refusesWhenUserLocked() {
        val um = context.getSystemService(UserManager::class.java)!!
        shadowOf(um).setUserUnlocked(false)
        val thrown = runCatching { vault.markDbEncrypted() }.exceptionOrNull()
        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun ensureProvisioned_createsCryptoDirAndWrappedFile() {
        val crypto = File(context.filesDir, DbPassphraseVault.DIR)
        assertThat(crypto.exists()).isFalse()
        vault.ensureProvisioned()
        assertThat(crypto.isDirectory).isTrue()
        val wrapped = File(crypto, DbPassphraseVault.WRAPPED_NAME)
        assertThat(wrapped.isFile).isTrue()
        assertThat(wrapped.length()).isGreaterThan(0L)
    }
}
