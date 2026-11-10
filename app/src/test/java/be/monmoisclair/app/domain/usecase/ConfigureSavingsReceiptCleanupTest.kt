package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.data.widget.CarnetWidgetSink
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfigureSavingsReceiptCleanupTest {

    private lateinit var appContext: Context
    private lateinit var db: AppDatabase
    private lateinit var useCase: ConfigureSavingsUseCase

    @Before
    fun setUp() {
        AppClock.clock = Clock.fixed(Instant.parse("2026-11-05T10:00:00Z"), ZoneId.of("Europe/Brussels"))
        appContext = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = MutatingRepository(
            db = db,
            backupScheduler = BackupScheduler {},
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) = Unit
            },
            widgetSink = CarnetWidgetSink.Noop
        )
        val addEntry = AddEntryUseCase(
            monthCycleEngine = be.monmoisclair.app.domain.MonthCycleEngine(db.monthLedgerDao(), repo),
            mutatingRepository = repo
        )
        useCase = ConfigureSavingsUseCase(
            appContext,
            addEntry = addEntry,
            mutatingRepository = repo,
            instanceDao = db.instanceDao(),
            templateDao = db.templateDao(),
            appMetaDao = db.appMetaDao(),
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) = Unit
            }
        )
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(ZoneId.of("Europe/Brussels"))
        db.close()
    }

    @Test
    fun remove_deletesLocalReceiptsOfRemovedTodoRows() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-savings",
                label = ConfigureSavingsUseCase.LABEL,
                amountCents = 15_000L,
                dayOfMonth = 8,
                active = true,
                intervalMonths = 1,
                anchorYearMonth = "2026-10",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.appMetaDao().upsert(AppMetaEntity(AppMetaKeys.SAVINGS_TEMPLATE_ID, "tpl-savings"))
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-11", "2026-11", createdAtEpochMs = 1L))
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-12", "2026-12", createdAtEpochMs = 1L))
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp:2026-11:tpl-savings",
                ledgerId = "2026-11",
                templateId = "tpl-savings",
                label = ConfigureSavingsUseCase.LABEL,
                amountCents = 15_000L,
                dayOfMonth = 8,
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                receiptRelativePath = "receipts/savings-now.webp",
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp:2026-12:tpl-savings",
                ledgerId = "2026-12",
                templateId = "tpl-savings",
                label = ConfigureSavingsUseCase.LABEL,
                amountCents = 15_000L,
                dayOfMonth = 8,
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                receiptRelativePath = "receipts/savings-next.pdf",
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        val receiptsDir = java.io.File(appContext.filesDir, "receipts").apply { mkdirs() }
        java.io.File(receiptsDir, "savings-now.webp").writeText("now")
        java.io.File(receiptsDir, "savings-next.pdf").writeText("next")

        assertThat(useCase.remove()).isEqualTo(ConfigureSavingsResult.Removed)

        assertThat(java.io.File(receiptsDir, "savings-now.webp").exists()).isFalse()
        assertThat(java.io.File(receiptsDir, "savings-next.pdf").exists()).isFalse()
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-savings")).isNull()
        assertThat(db.instanceDao().getExpenseById("exp:2026-12:tpl-savings")).isNull()
    }
}
