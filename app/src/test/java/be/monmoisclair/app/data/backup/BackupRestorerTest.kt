package be.monmoisclair.app.data.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.BackupHealth
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class BackupRestorerTest {

    private lateinit var db: AppDatabase
    private lateinit var codec: BackupJsonCodec
    private lateinit var healthStore: BackupHealthStore
    private lateinit var restorer: BackupRestorer
    private val reconcileCalls = AtomicInteger(0)
    private val backupCalls = AtomicInteger(0)

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        codec = BackupJsonCodec()
        healthStore = BackupHealthStore(db.appMetaDao())
        reconcileCalls.set(0)
        backupCalls.set(0)

        val safStore = SafBackupStore(
            context = context,
            appMetaDao = db.appMetaDao(),
            codec = codec,
            rotator = BackupRotator(),
            healthStore = healthStore
        )

        restorer = BackupRestorer(
            context = context,
            db = db,
            safBackupStore = safStore,
            codec = codec,
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() {
                    reconcileCalls.incrementAndGet()
                }

                override suspend fun cancelFor(expenseId: String) = Unit
            },
            backupScheduler = BackupScheduler { backupCalls.incrementAndGet() },
            healthStore = healthStore
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun restore_replacesData_preservesLocalSafUri_andTriggersReconcileBackup() = runBlocking {
        seedCurrentPhoneState(
            ledgerId = "2026-09",
            expenseId = "old-exp",
            safUri = "content://com.android.externalstorage.documents/tree/local-saf"
        )

        val backupPayload = BackupPayload(
            ledgers = listOf(MonthLedgerEntity("2026-10", "2026-10", 99L)),
            incomeTemplates = emptyList(),
            expenseTemplates = emptyList(),
            incomeInstances = emptyList(),
            expenseInstances = listOf(
                ExpenseInstanceEntity(
                    id = "new-exp",
                    ledgerId = "2026-10",
                    templateId = null,
                    label = "Loyer",
                    amountCents = 47_000L,
                    dayOfMonth = 5,
                    status = ExpenseStatus.TODO,
                    doneAtEpochMs = null,
                    remindJ3 = true,
                    remindJd = true,
                    alarmJ3RequestCode = null,
                    alarmJdRequestCode = null,
                    createdAtEpochMs = 99L
                )
            ),
            appMeta = listOf(
                AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, "1"),
                AppMetaEntity(AppMetaKeys.SAF_TREE_URI, "content://old-phone/tree/other"),
                AppMetaEntity(AppMetaKeys.BACKUP_HEALTH, BackupHealth.BROKEN.name)
            )
        )
        val document = codec.buildDocument(1, 1_234L, "Europe/Brussels", backupPayload)

        val result = restorer.restoreFromDocument(document)
        assertThat(result).isInstanceOf(RestoreResult.Success::class.java)

        assertThat(db.monthLedgerDao().getById("2026-09")).isNull()
        assertThat(db.monthLedgerDao().getById("2026-10")).isNotNull()
        assertThat(db.instanceDao().getExpenseById("old-exp")).isNull()
        assertThat(db.instanceDao().getExpenseById("new-exp")!!.amountCents).isEqualTo(47_000L)

        // URI locale conservée (pas celle du backup d'un autre téléphone)
        assertThat(db.appMetaDao().get(AppMetaKeys.SAF_TREE_URI)?.value)
            .isEqualTo("content://com.android.externalstorage.documents/tree/local-saf")
        assertThat(healthStore.read()).isEqualTo(BackupHealth.OK)
        assertThat(db.appMetaDao().get(AppMetaKeys.LAST_BACKUP_CHECKSUM_SHA256)?.value)
            .isEqualTo(document.checksumSha256)

        assertThat(reconcileCalls.get()).isEqualTo(1)
        assertThat(backupCalls.get()).isEqualTo(1)
    }

    @Test
    fun restore_withInvalidChecksum_doesNotTouchDatabase() = runBlocking {
        seedCurrentPhoneState(ledgerId = "2026-08", expenseId = "keep-me", safUri = null)

        val document = codec.buildDocument(
            1,
            1L,
            "Europe/Brussels",
            BackupPayload(
                ledgers = listOf(MonthLedgerEntity("2099-01", "2099-01", 1L)),
                incomeTemplates = emptyList(),
                expenseTemplates = emptyList(),
                incomeInstances = emptyList(),
                expenseInstances = emptyList(),
                appMeta = emptyList()
            )
        )
        val tampered = document.copy(checksumSha256 = "0".repeat(64))

        val result = restorer.restoreFromDocument(tampered)
        assertThat(result).isInstanceOf(RestoreResult.InvalidChecksum::class.java)

        assertThat(db.monthLedgerDao().getById("2026-08")).isNotNull()
        assertThat(db.monthLedgerDao().getById("2099-01")).isNull()
        assertThat(db.instanceDao().getExpenseById("keep-me")).isNotNull()
        assertThat(reconcileCalls.get()).isEqualTo(0)
        assertThat(backupCalls.get()).isEqualTo(0)
    }

    @Test
    fun restore_withUnsupportedSchema_doesNotTouchDatabase() = runBlocking {
        seedCurrentPhoneState(ledgerId = "2026-07", expenseId = "keep-schema", safUri = null)

        val document = codec.buildDocument(
            1,
            1L,
            "Europe/Brussels",
            BackupPayload(
                ledgers = emptyList(),
                incomeTemplates = emptyList(),
                expenseTemplates = emptyList(),
                incomeInstances = emptyList(),
                expenseInstances = emptyList(),
                appMeta = emptyList()
            )
        ).copy(schemaVersion = 99)

        val result = restorer.restoreFromDocument(document)
        assertThat(result).isInstanceOf(RestoreResult.UnsupportedSchema::class.java)
        assertThat((result as RestoreResult.UnsupportedSchema).version).isEqualTo(99)

        assertThat(db.monthLedgerDao().getById("2026-07")).isNotNull()
        assertThat(reconcileCalls.get()).isEqualTo(0)
    }

    @Test
    fun restoreFromUri_validFile_appliesPayload() = runBlocking {
        seedCurrentPhoneState("2026-01", "old", safUri = "content://tree/keep")

        val payload = BackupPayload(
            ledgers = listOf(MonthLedgerEntity("2026-11", "2026-11", 2L)),
            incomeTemplates = emptyList(),
            expenseTemplates = emptyList(),
            incomeInstances = emptyList(),
            expenseInstances = emptyList(),
            appMeta = listOf(AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, "1"))
        )
        val document = codec.buildDocument(1, 50L, "Europe/Brussels", payload)
        val json = codec.encodeDocument(document)

        val context: Context = ApplicationProvider.getApplicationContext()
        val file = context.filesDir.resolve("restore-test.json")
        file.writeText(json, Charsets.UTF_8)
        val uri = android.net.Uri.fromFile(file)

        val result = restorer.restoreFromUri(uri)
        assertThat(result).isInstanceOf(RestoreResult.Success::class.java)
        assertThat(db.monthLedgerDao().getById("2026-11")).isNotNull()
        assertThat(db.appMetaDao().get(AppMetaKeys.SAF_TREE_URI)?.value)
            .isEqualTo("content://tree/keep")
    }

    @Test
    fun restoreFromLatest_withoutSaf_returnsNotConfigured() = runBlocking {
        val result = restorer.restoreFromLatest()
        assertThat(result).isEqualTo(RestoreResult.NotConfigured)
        assertThat(reconcileCalls.get()).isEqualTo(0)
    }

    private suspend fun seedCurrentPhoneState(
        ledgerId: String,
        expenseId: String,
        safUri: String?
    ) {
        db.monthLedgerDao().insert(MonthLedgerEntity(ledgerId, ledgerId, 1L))
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = expenseId,
                ledgerId = ledgerId,
                templateId = null,
                label = "Ancien",
                amountCents = 1_000L,
                dayOfMonth = 1,
                status = ExpenseStatus.DONE,
                doneAtEpochMs = 1L,
                remindJ3 = false,
                remindJd = false,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        db.appMetaDao().upsert(
            AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, AppMetaKeys.CURRENT_SCHEMA_VERSION)
        )
        if (safUri != null) {
            db.appMetaDao().upsert(AppMetaEntity(AppMetaKeys.SAF_TREE_URI, safUri))
        }
        db.appMetaDao().upsert(
            AppMetaEntity(AppMetaKeys.BACKUP_HEALTH, BackupHealth.OK.name)
        )
    }
}
