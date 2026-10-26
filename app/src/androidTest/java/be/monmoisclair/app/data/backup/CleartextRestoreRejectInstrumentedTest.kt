package be.monmoisclair.app.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import be.monmoisclair.app.data.crypto.ShareSecretVault
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.widget.CarnetWidgetSink
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.BackupHealth
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Restore JSON clair refusée sans opt-in — filet instrumenté (Keystore device).
 * Pas de Compose / Hilt : setup Room in-memory local.
 */
@RunWith(AndroidJUnit4::class)
class CleartextRestoreRejectInstrumentedTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var restorer: BackupRestorer

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val codec = BackupJsonCodec()
        val healthStore = BackupHealthStore(db.appMetaDao())
        val shareVault = ShareSecretVault(context)
        val safStore = SafBackupStore(
            context = context,
            appMetaDao = db.appMetaDao(),
            codec = codec,
            rotator = BackupRotator(),
            healthStore = healthStore,
            shareSecretVault = shareVault
        )
        restorer = BackupRestorer(
            context = context,
            db = db,
            safBackupStore = safStore,
            codec = codec,
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) = Unit
            },
            backupScheduler = BackupScheduler { },
            healthStore = healthStore,
            widgetSink = CarnetWidgetSink.Noop,
            shareSecretVault = shareVault
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun restoreFromUri_cleartextJson_rejectedByDefault() = runBlocking {
        db.monthLedgerDao().insert(
            MonthLedgerEntity("2026-01", "2026-01", createdAtEpochMs = 1L)
        )
        db.appMetaDao().upsert(
            AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, AppMetaKeys.CURRENT_SCHEMA_VERSION)
        )
        db.appMetaDao().upsert(
            AppMetaEntity(AppMetaKeys.BACKUP_HEALTH, BackupHealth.OK.name)
        )

        val payload = BackupPayload(
            ledgers = listOf(MonthLedgerEntity("2026-11", "2026-11", createdAtEpochMs = 2L)),
            incomeTemplates = emptyList(),
            expenseTemplates = emptyList(),
            incomeInstances = emptyList(),
            expenseInstances = emptyList(),
            appMeta = listOf(
                AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, AppMetaKeys.CURRENT_SCHEMA_VERSION)
            )
        )
        val codec = BackupJsonCodec()
        val document = codec.buildDocument(
            AppMetaKeys.CURRENT_SCHEMA_VERSION.toInt(),
            50L,
            "Europe/Brussels",
            payload
        )
        val file = context.cacheDir.resolve("androidtest-cleartext.json")
        file.writeText(codec.encodeDocument(document), Charsets.UTF_8)

        val rejected = restorer.restoreFromUri(Uri.fromFile(file))
        assertThat(rejected).isEqualTo(RestoreResult.CleartextRejected)
        assertThat(db.monthLedgerDao().getById("2026-11")).isNull()
        assertThat(db.monthLedgerDao().getById("2026-01")).isNotNull()
    }
}
