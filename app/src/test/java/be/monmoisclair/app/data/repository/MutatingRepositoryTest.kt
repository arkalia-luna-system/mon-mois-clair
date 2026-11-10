package be.monmoisclair.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.domain.MonthDayClamp
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
import java.time.YearMonth
import java.util.concurrent.atomic.AtomicInteger
import be.monmoisclair.app.data.widget.CarnetWidgetSink

@RunWith(RobolectricTestRunner::class)
class MutatingRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: MutatingRepository
    private val backupCalls = AtomicInteger(0)
    private val reconcileCalls = AtomicInteger(0)
    private val widgetRefreshCalls = AtomicInteger(0)

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backupCalls.set(0)
        reconcileCalls.set(0)
        widgetRefreshCalls.set(0)
        repo = MutatingRepository(
            db = db,
            backupScheduler = BackupScheduler { backupCalls.incrementAndGet() },
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() {
                    reconcileCalls.incrementAndGet()
                }

                override suspend fun cancelFor(expenseId: String) = Unit
            },
            widgetSink = object : CarnetWidgetSink {
                override suspend fun refresh() {
                    widgetRefreshCalls.incrementAndGet()
                }
            }
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun mutate_persistsLedgerAndExpense_andTriggersBackupAndReconcile() = runBlocking {
        val ym = YearMonth.of(2026, 2)
        val day = MonthDayClamp.clamp(31, ym)

        repo.mutate {
            monthLedgerDao.insert(
                MonthLedgerEntity(
                    id = ym.toString(),
                    yearMonth = ym.toString(),
                    createdAtEpochMs = 1L
                )
            )
            instanceDao.insertExpense(
                ExpenseInstanceEntity(
                    id = "exp-1",
                    ledgerId = ym.toString(),
                    templateId = "tpl-loyer",
                    label = "Loyer",
                    amountCents = 47_000L,
                    dayOfMonth = day,
                    status = ExpenseStatus.TODO,
                    doneAtEpochMs = null,
                    remindJ3 = true,
                    remindJd = true,
                    alarmJ3RequestCode = null,
                    alarmJdRequestCode = null,
                    createdAtEpochMs = 1L
                )
            )
            appMetaDao.upsert(
                AppMetaEntity(
                    key = AppMetaKeys.BACKUP_HEALTH,
                    value = BackupHealth.OK.name
                )
            )
        }

        val expense = db.instanceDao().getExpenseById("exp-1")
        assertThat(expense).isNotNull()
        assertThat(expense!!.dayOfMonth).isEqualTo(28)
        assertThat(expense.status).isEqualTo(ExpenseStatus.TODO)
        assertThat(expense.amountCents).isEqualTo(47_000L)

        assertThat(backupCalls.get()).isEqualTo(1)
        assertThat(reconcileCalls.get()).isEqualTo(1)
        assertThat(widgetRefreshCalls.get()).isEqualTo(1)
    }

    @Test
    fun mutate_whenBlockThrows_doesNotBackupOrReconcile_andRollsBack() = runBlocking {
        runCatching {
            repo.mutate {
                monthLedgerDao.insert(
                    MonthLedgerEntity(
                        id = "2026-10",
                        yearMonth = "2026-10",
                        createdAtEpochMs = 1L
                    )
                )
                error("simulated failure after insert")
            }
        }

        assertThat(db.monthLedgerDao().getById("2026-10")).isNull()
        assertThat(backupCalls.get()).isEqualTo(0)
        assertThat(reconcileCalls.get()).isEqualTo(0)
        assertThat(widgetRefreshCalls.get()).isEqualTo(0)
    }

    @Test
    fun mutate_canSkipReminderReconcile() = runBlocking {
        repo.mutate(rescheduleReminders = false) {
            appMetaDao.upsert(
                AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, AppMetaKeys.CURRENT_SCHEMA_VERSION)
            )
        }
        assertThat(backupCalls.get()).isEqualTo(1)
        assertThat(reconcileCalls.get()).isEqualTo(0)
        assertThat(widgetRefreshCalls.get()).isEqualTo(1)
    }

    @Test
    fun mutate_sideEffectsWhenFalse_skipsBackupAndReconcile() = runBlocking {
        val result = repo.mutate(sideEffectsWhen = { false }) {
            appMetaDao.upsert(
                AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, AppMetaKeys.CURRENT_SCHEMA_VERSION)
            )
            "noop"
        }
        assertThat(result).isEqualTo("noop")
        assertThat(db.appMetaDao().get(AppMetaKeys.SCHEMA_VERSION)?.value)
            .isEqualTo(AppMetaKeys.CURRENT_SCHEMA_VERSION)
        assertThat(backupCalls.get()).isEqualTo(0)
        assertThat(reconcileCalls.get()).isEqualTo(0)
        assertThat(widgetRefreshCalls.get()).isEqualTo(1)
    }

    @Test
    fun expenseStatus_roundTripsThroughRoomAsEnum() = runBlocking {
        repo.mutate(rescheduleReminders = false) {
            monthLedgerDao.insert(
                MonthLedgerEntity("2026-03", "2026-03", createdAtEpochMs = 1L)
            )
            instanceDao.insertExpense(
                ExpenseInstanceEntity(
                    id = "exp-done",
                    ledgerId = "2026-03",
                    templateId = null,
                    label = "Courses",
                    amountCents = 2_500L,
                    dayOfMonth = 12,
                    status = ExpenseStatus.DONE,
                    doneAtEpochMs = 99L,
                    remindJ3 = false,
                    remindJd = false,
                    alarmJ3RequestCode = null,
                    alarmJdRequestCode = null,
                    createdAtEpochMs = 1L
                )
            )
        }

        val loaded = db.instanceDao().getExpenseById("exp-done")
        assertThat(loaded!!.status).isEqualTo(ExpenseStatus.DONE)
        assertThat(db.instanceDao().expensesByStatus("2026-03", ExpenseStatus.DONE)).hasSize(1)
        assertThat(db.instanceDao().expensesByStatus("2026-03", ExpenseStatus.TODO)).isEmpty()
    }

    @Test
    fun mutate_refreshesWidget_evenWhenReminderReconcileFails() = runBlocking {
        val failingRepo = MutatingRepository(
            db = db,
            backupScheduler = BackupScheduler { backupCalls.incrementAndGet() },
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() {
                    reconcileCalls.incrementAndGet()
                    error("boom")
                }

                override suspend fun cancelFor(expenseId: String) = Unit
            },
            widgetSink = object : CarnetWidgetSink {
                override suspend fun refresh() {
                    widgetRefreshCalls.incrementAndGet()
                }
            }
        )

        val failure = runCatching {
            failingRepo.mutate {
                appMetaDao.upsert(
                    AppMetaEntity(AppMetaKeys.SCHEMA_VERSION, AppMetaKeys.CURRENT_SCHEMA_VERSION)
                )
            }
        }.exceptionOrNull()

        assertThat(failure).isNotNull()
        assertThat(backupCalls.get()).isEqualTo(1)
        assertThat(reconcileCalls.get()).isEqualTo(1)
        assertThat(widgetRefreshCalls.get()).isEqualTo(1)
    }
}
