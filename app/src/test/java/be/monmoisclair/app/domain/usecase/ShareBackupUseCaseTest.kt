package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupCoordinator
import be.monmoisclair.app.data.backup.BackupJsonCodec
import be.monmoisclair.app.data.backup.BackupRotator
import be.monmoisclair.app.data.backup.BackupSecurityArchive
import be.monmoisclair.app.data.backup.BackupSnapshotReader
import be.monmoisclair.app.data.backup.BackupHealthStore
import be.monmoisclair.app.data.backup.SafBackupStore
import be.monmoisclair.app.data.crypto.BackupEnvelope
import be.monmoisclair.app.data.crypto.ShareSecretVault
import be.monmoisclair.app.data.db.AppDatabase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShareBackupUseCaseTest {

    @get:Rule
    val tmp: TemporaryFolder = TemporaryFolder()

    private lateinit var db: AppDatabase
    private lateinit var useCase: ShareBackupUseCase
    private lateinit var vault: ShareSecretVault

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        vault = ShareSecretVault(context)
        val healthStore = BackupHealthStore(db.appMetaDao())
        val codec = BackupJsonCodec()
        val saf = SafBackupStore(
            context = context,
            appMetaDao = db.appMetaDao(),
            codec = codec,
            rotator = BackupRotator(),
            healthStore = healthStore,
            shareSecretVault = vault
        )
        val coordinator = BackupCoordinator(
            snapshotReader = BackupSnapshotReader(
                db.monthLedgerDao(),
                db.templateDao(),
                db.instanceDao(),
                db.appMetaDao(),
                db.pendingLetterDao(),
                db.expenseFeeEventDao()
            ),
            codec = codec,
            safBackupStore = saf,
            healthStore = healthStore
        )
        useCase = ShareBackupUseCase(context, coordinator, saf, vault)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun withoutFolder_returnsFolderRequired() = runBlocking {
        val result = useCase()
        assertThat(result).isEqualTo(
            ShareBackupResult.Failed(ShareBackupMessages.FOLDER_REQUIRED)
        )
    }

    @Test
    fun packAndSeal_mirrorsSharePath_producesMmcMagic() {
        // Miroir du cœur de ShareBackupUseCase (ZIP → seal MMC1) sans SAF Documents.
        val zipTmp = tmp.newFile("pack-tmp.zip")
        val packed = BackupSecurityArchive.packToFile(
            zipTmp,
            """{"schemaVersion":8,"payload":{}}""",
            emptyMap()
        )
        val secret = vault.getOrCreateSecretChars()
        try {
            val sealed = BackupEnvelope.seal(packed.file.readBytes(), secret)
            assertThat(BackupEnvelope.looksLikeBinary(sealed)).isTrue()
            assertThat(sealed.copyOfRange(0, 4).toString(Charsets.US_ASCII))
                .isEqualTo("MMC1")
            // Sans code : pas de JSON lisible dans le binaire.
            val asText = sealed.toString(Charsets.ISO_8859_1)
            assertThat(asText).doesNotContain("schemaVersion")
        } finally {
            secret.fill('\u0000')
            packed.file.delete()
        }
    }
}
