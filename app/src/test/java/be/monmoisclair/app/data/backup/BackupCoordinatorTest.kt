package be.monmoisclair.app.data.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.BackupHealth
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
class BackupCoordinatorTest {

    @get:Rule
    val tmp: TemporaryFolder = TemporaryFolder()

    private lateinit var db: AppDatabase
    private lateinit var healthStore: BackupHealthStore
    private lateinit var codec: BackupJsonCodec
    private lateinit var rotator: BackupRotator
    private lateinit var fileTree: FileSystemBackupTree

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        healthStore = BackupHealthStore(db.appMetaDao())
        codec = BackupJsonCodec()
        rotator = BackupRotator()
        fileTree = FileSystemBackupTree(tmp.root)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun runAutoBackup_withoutSaf_skipsAndKeepsNotConfigured() = runBlocking {
        val store = SafBackupStore(
            context = ApplicationProvider.getApplicationContext(),
            appMetaDao = db.appMetaDao(),
            codec = codec,
            rotator = rotator,
            healthStore = healthStore
        )
        val coordinator = BackupCoordinator(
            snapshotReader = BackupSnapshotReader(
                db.monthLedgerDao(),
                db.templateDao(),
                db.instanceDao(),
                db.appMetaDao()
            ),
            codec = codec,
            safBackupStore = store,
            healthStore = healthStore
        )

        val result = coordinator.runAutoBackup()
        assertThat(result).isEqualTo(BackupCoordinator.Result.SkippedNotConfigured)
        assertThat(healthStore.read()).isEqualTo(BackupHealth.NOT_CONFIGURED)
    }

    @Test
    fun fileTreeRotation_withSnapshot_roundTripsChecksumIntoHealth() = runBlocking {
        db.monthLedgerDao().insert(
            MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L)
        )
        db.appMetaDao().upsert(AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, "1"))

        val payload = BackupSnapshotReader(
            db.monthLedgerDao(),
            db.templateDao(),
            db.instanceDao(),
            db.appMetaDao()
        ).readPayload()

        val document = codec.buildDocument(1, 42L, "Europe/Brussels", payload)
        rotator.writeRotated(fileTree, codec.encodeDocument(document))
        healthStore.markSuccess(document.checksumSha256, 42L)

        val loaded = codec.decodeAndVerify(fileTree.readUtf8(BackupFileNames.V1))
        assertThat(loaded.checksumSha256).isEqualTo(document.checksumSha256)
        assertThat(loaded.payload.ledgers).hasSize(1)
        assertThat(healthStore.read()).isEqualTo(BackupHealth.OK)
        assertThat(db.appMetaDao().get(AppMetaKeys.LAST_BACKUP_CHECKSUM_SHA256)?.value)
            .isEqualTo(document.checksumSha256)
    }

    @Test
    fun healthStore_markBroken_isReadableByUiContract() = runBlocking {
        healthStore.markBroken()
        assertThat(healthStore.read()).isEqualTo(BackupHealth.BROKEN)
    }
}
