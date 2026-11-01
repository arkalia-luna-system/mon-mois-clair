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
import be.monmoisclair.app.data.widget.CarnetWidgetSink

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

        val shareVault = be.monmoisclair.app.data.crypto.ShareSecretVault(context)
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
                override suspend fun reconcileAll() {
                    reconcileCalls.incrementAndGet()
                }

                override suspend fun cancelFor(expenseId: String) = Unit
            },
            backupScheduler = BackupScheduler { backupCalls.incrementAndGet() },
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
    fun restore_replacesData_preservesLocalSafUri_andTriggersReconcileBackup() = runBlocking {
        seedCurrentPhoneState(
            ledgerId = "2026-09",
            expenseId = "old-exp",
            safUri = "content://com.android.externalstorage.documents/tree/local-saf"
        )

        val backupPayload = BackupPayload(
            ledgers = listOf(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 99L)),
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
                AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, AppMetaKeys.CURRENT_SCHEMA_VERSION),
                AppMetaEntity(AppMetaKeys.SAF_TREE_URI, "content://old-phone/tree/other"),
                AppMetaEntity(AppMetaKeys.BACKUP_HEALTH, BackupHealth.BROKEN.name)
            )
        )
        val document = codec.buildDocument(
            AppMetaKeys.CURRENT_SCHEMA_VERSION.toInt(),
            1_234L,
            "Europe/Brussels",
            backupPayload
        )

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
            AppMetaKeys.CURRENT_SCHEMA_VERSION.toInt(),
            1L,
            "Europe/Brussels",
            BackupPayload(
                ledgers = listOf(MonthLedgerEntity("2099-01", "2099-01", createdAtEpochMs = 1L)),
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
    fun restore_acceptsLegacySchema4_andBumpsMetaToCurrent() = runBlocking {
        seedCurrentPhoneState(ledgerId = "2026-09", expenseId = "old-schema4", safUri = null)

        val payload = BackupPayload(
            ledgers = listOf(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 5L)),
            incomeTemplates = emptyList(),
            expenseTemplates = emptyList(),
            incomeInstances = emptyList(),
            expenseInstances = listOf(
                ExpenseInstanceEntity(
                    id = "legacy-s4",
                    ledgerId = "2026-10",
                    templateId = null,
                    label = "Eau",
                    amountCents = 4_000L,
                    dayOfMonth = 4,
                    status = ExpenseStatus.TODO,
                    doneAtEpochMs = null,
                    remindJ3 = true,
                    remindJd = true,
                    paymentNote = "note-s4",
                    receiptRelativePath = "",
                    alarmJ3RequestCode = null,
                    alarmJdRequestCode = null,
                    createdAtEpochMs = 5L
                )
            ),
            appMeta = listOf(AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, "4"))
        )
        val document = codec.buildDocument(4, 5L, "Europe/Brussels", payload)

        val result = restorer.restoreFromDocument(document)
        assertThat(result).isInstanceOf(RestoreResult.Success::class.java)
        assertThat(db.instanceDao().getExpenseById("legacy-s4")?.paymentNote).isEqualTo("note-s4")
        assertThat(db.appMetaDao().get(AppMetaKeys.SCHEMA_VERSION)?.value)
            .isEqualTo(AppMetaKeys.CURRENT_SCHEMA_VERSION)
    }

    @Test
    fun restore_acceptsLegacySchema3_andBumpsMetaToCurrent() = runBlocking {
        seedCurrentPhoneState(ledgerId = "2026-08", expenseId = "old-schema3", safUri = null)

        val payload = BackupPayload(
            ledgers = listOf(MonthLedgerEntity("2026-11", "2026-11", createdAtEpochMs = 4L)),
            incomeTemplates = emptyList(),
            expenseTemplates = emptyList(),
            incomeInstances = emptyList(),
            expenseInstances = listOf(
                ExpenseInstanceEntity(
                    id = "legacy-s3",
                    ledgerId = "2026-11",
                    templateId = null,
                    label = "Gaz",
                    amountCents = 5_000L,
                    dayOfMonth = 8,
                    status = ExpenseStatus.TODO,
                    doneAtEpochMs = null,
                    remindJ3 = true,
                    remindJd = true,
                    paymentNote = "note-s3",
                    receiptRelativePath = "receipts/s3.webp",
                    alarmJ3RequestCode = null,
                    alarmJdRequestCode = null,
                    createdAtEpochMs = 4L
                )
            ),
            appMeta = listOf(AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, "3"))
        )
        val document = codec.buildDocument(3, 4L, "Europe/Brussels", payload)

        val result = restorer.restoreFromDocument(document)
        assertThat(result).isInstanceOf(RestoreResult.Success::class.java)
        assertThat(db.instanceDao().getExpenseById("legacy-s3")?.paymentNote).isEqualTo("note-s3")
        assertThat(db.appMetaDao().get(AppMetaKeys.SCHEMA_VERSION)?.value)
            .isEqualTo(AppMetaKeys.CURRENT_SCHEMA_VERSION)
    }

    @Test
    fun restore_acceptsLegacySchema2_andBumpsMetaToCurrent() = runBlocking {
        seedCurrentPhoneState(ledgerId = "2026-07", expenseId = "old-schema2", safUri = null)

        val payload = BackupPayload(
            ledgers = listOf(MonthLedgerEntity("2026-12", "2026-12", createdAtEpochMs = 3L)),
            incomeTemplates = emptyList(),
            expenseTemplates = emptyList(),
            incomeInstances = emptyList(),
            expenseInstances = listOf(
                ExpenseInstanceEntity(
                    id = "legacy-exp",
                    ledgerId = "2026-12",
                    templateId = null,
                    label = "Eau",
                    amountCents = 7_450L,
                    dayOfMonth = 10,
                    status = ExpenseStatus.TODO,
                    doneAtEpochMs = null,
                    remindJ3 = true,
                    remindJd = true,
                    alarmJ3RequestCode = null,
                    alarmJdRequestCode = null,
                    createdAtEpochMs = 3L
                )
            ),
            appMeta = listOf(AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, "2"))
        )
        val document = codec.buildDocument(2, 3L, "Europe/Brussels", payload)

        val result = restorer.restoreFromDocument(document)
        assertThat(result).isInstanceOf(RestoreResult.Success::class.java)
        assertThat(db.instanceDao().getExpenseById("legacy-exp")?.paymentNote).isEqualTo("")
        assertThat(db.appMetaDao().get(AppMetaKeys.SCHEMA_VERSION)?.value)
            .isEqualTo(AppMetaKeys.CURRENT_SCHEMA_VERSION)
    }

    @Test
    fun restoreFromUri_validFile_appliesPayload() = runBlocking {
        seedCurrentPhoneState("2026-01", "old", safUri = "content://tree/keep")

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
        val document = codec.buildDocument(
            AppMetaKeys.CURRENT_SCHEMA_VERSION.toInt(),
            50L,
            "Europe/Brussels",
            payload
        )
        val json = codec.encodeDocument(document)

        val context: Context = ApplicationProvider.getApplicationContext()
        val file = context.filesDir.resolve("restore-test.json")
        file.writeText(json, Charsets.UTF_8)
        val uri = android.net.Uri.fromFile(file)

        // JSON clair : refusé par défaut (DB inchangée).
        val rejected = restorer.restoreFromUri(uri)
        assertThat(rejected).isEqualTo(RestoreResult.CleartextRejected)
        assertThat(db.monthLedgerDao().getById("2026-11")).isNull()
        assertThat(db.monthLedgerDao().getById("2026-01")).isNotNull()

        // 6.99 : grâce close — même avec allowCleartextLegacy, toujours rejeté.
        assertThat(CleartextLegacyGrace.OPT_IN_AVAILABLE).isFalse()
        val stillRejected = restorer.restoreFromUri(uri, allowCleartextLegacy = true)
        assertThat(stillRejected).isEqualTo(RestoreResult.CleartextRejected)
        assertThat(db.monthLedgerDao().getById("2026-11")).isNull()
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
        db.monthLedgerDao().insert(MonthLedgerEntity(ledgerId, ledgerId, createdAtEpochMs = 1L))
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
