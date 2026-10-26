package be.monmoisclair.app.data.crypto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Chemins sans SQLCipher natif : marqueur déjà posé / pas de DB claire.
 * La migration ATTACH réelle est exercée sur device (assembleRelease).
 */
@RunWith(RobolectricTestRunner::class)
class EncryptedDbMigratorTest {

    private lateinit var context: Context
    private lateinit var vault: DbPassphraseVault

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        File(context.filesDir, DbPassphraseVault.DIR).deleteRecursively()
        context.getDatabasePath(DbPassphraseVault.DB_NAME).let { db ->
            db.delete()
            File(db.path + "-wal").delete()
            File(db.path + "-shm").delete()
        }
        vault = DbPassphraseVault(context)
    }

    @Test
    fun migrateIfNeeded_noDb_returnsNoPlaintext() {
        val outcome = EncryptedDbMigrator.migrateIfNeeded(context, vault)
        assertThat(outcome).isEqualTo(EncryptedDbMigrator.MigrationOutcome.NoPlaintextDb)
    }

    @Test
    fun migrateIfNeeded_markerPresent_alreadyEncrypted() {
        vault.ensureProvisioned()
        vault.markDbEncrypted()
        // Fichier factice pour ne pas retomber sur NoPlaintextDb.
        context.getDatabasePath(DbPassphraseVault.DB_NAME).apply {
            parentFile?.mkdirs()
            writeText("not-a-real-sqlite")
        }
        val outcome = EncryptedDbMigrator.migrateIfNeeded(context, vault)
        assertThat(outcome).isEqualTo(EncryptedDbMigrator.MigrationOutcome.AlreadyEncrypted)
    }
}
