package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class ToggleExpenseDoneUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: ToggleExpenseDoneUseCase
    private val backupCalls = AtomicInteger(0)
    private val reconcileCalls = AtomicInteger(0)
    private val brussels = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backupCalls.set(0)
        reconcileCalls.set(0)

        val repo = MutatingRepository(
            db = db,
            backupScheduler = BackupScheduler { backupCalls.incrementAndGet() },
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() {
                    reconcileCalls.incrementAndGet()
                }

                override suspend fun cancelFor(expenseId: String) = Unit
            }
        )
        useCase = ToggleExpenseDoneUseCase(
            instanceDao = db.instanceDao(),
            mutatingRepository = repo
        )

        AppClock.clock = Clock.fixed(Instant.parse("2026-11-02T10:00:00Z"), brussels)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    @Test
    fun toggle_todoToDone_setsDoneAt_andTriggersBackupReconcile() = runBlocking {
        seedExpense(
            id = "exp-1",
            ledgerId = "2026-11",
            status = ExpenseStatus.TODO
        )

        val result = useCase("exp-1")
        assertThat(result).isInstanceOf(ToggleExpenseDoneResult.Toggled::class.java)
        val toggled = result as ToggleExpenseDoneResult.Toggled
        assertThat(toggled.previousStatus).isEqualTo(ExpenseStatus.TODO)
        assertThat(toggled.newStatus).isEqualTo(ExpenseStatus.DONE)
        assertThat(toggled.doneAtEpochMs).isEqualTo(AppClock.nowEpochMs())
        assertThat(toggled.ledgerId).isEqualTo("2026-11")

        val stored = db.instanceDao().getExpenseById("exp-1")!!
        assertThat(stored.status).isEqualTo(ExpenseStatus.DONE)
        assertThat(stored.doneAtEpochMs).isEqualTo(AppClock.nowEpochMs())
        assertThat(backupCalls.get()).isEqualTo(1)
        assertThat(reconcileCalls.get()).isEqualTo(1)
    }

    @Test
    fun toggle_doneToTodo_clearsDoneAt() = runBlocking {
        seedExpense(
            id = "exp-2",
            ledgerId = "2026-11",
            status = ExpenseStatus.DONE,
            doneAt = 1_000L
        )

        val result = useCase("exp-2") as ToggleExpenseDoneResult.Toggled
        assertThat(result.previousStatus).isEqualTo(ExpenseStatus.DONE)
        assertThat(result.newStatus).isEqualTo(ExpenseStatus.TODO)
        assertThat(result.doneAtEpochMs).isNull()

        val stored = db.instanceDao().getExpenseById("exp-2")!!
        assertThat(stored.status).isEqualTo(ExpenseStatus.TODO)
        assertThat(stored.doneAtEpochMs).isNull()
    }

    @Test
    fun toggle_worksOnPreviousMonth() = runBlocking {
        // Aujourd'hui = novembre ; on coche une dépense d'octobre oubliée.
        seedExpense(
            id = "exp-oct-28",
            ledgerId = "2026-10",
            status = ExpenseStatus.TODO,
            dayOfMonth = 28
        )

        val result = useCase("exp-oct-28") as ToggleExpenseDoneResult.Toggled
        assertThat(result.ledgerId).isEqualTo("2026-10")
        assertThat(result.newStatus).isEqualTo(ExpenseStatus.DONE)
        assertThat(db.instanceDao().getExpenseById("exp-oct-28")!!.status)
            .isEqualTo(ExpenseStatus.DONE)
    }

    @Test
    fun toggle_unknownId_returnsNotFound_withoutSideEffects() = runBlocking {
        val result = useCase("does-not-exist")
        assertThat(result).isEqualTo(ToggleExpenseDoneResult.NotFound)
        assertThat(db.instanceDao().getExpenseById("does-not-exist")).isNull()
        assertThat(backupCalls.get()).isEqualTo(0)
        assertThat(reconcileCalls.get()).isEqualTo(0)
    }

    @Test
    fun toggle_futurePreviewMonth_isBlocked_withoutSideEffects() = runBlocking {
        // Aujourd'hui = novembre ; décembre = aperçu.
        seedExpense(
            id = "exp-dec",
            ledgerId = "2026-12",
            status = ExpenseStatus.TODO
        )

        val result = useCase("exp-dec")
        assertThat(result).isEqualTo(ToggleExpenseDoneResult.FuturePreviewBlocked)
        assertThat(db.instanceDao().getExpenseById("exp-dec")!!.status)
            .isEqualTo(ExpenseStatus.TODO)
        assertThat(backupCalls.get()).isEqualTo(0)
        assertThat(reconcileCalls.get()).isEqualTo(0)
    }

    @Test
    fun toggle_roundTrip_twice_restoresOriginalTodo() = runBlocking {
        seedExpense("exp-rt", "2026-09", ExpenseStatus.TODO)
        useCase("exp-rt")
        useCase("exp-rt")
        val stored = db.instanceDao().getExpenseById("exp-rt")!!
        assertThat(stored.status).isEqualTo(ExpenseStatus.TODO)
        assertThat(stored.doneAtEpochMs).isNull()
        assertThat(backupCalls.get()).isEqualTo(2)
    }

    private suspend fun seedExpense(
        id: String,
        ledgerId: String,
        status: ExpenseStatus,
        dayOfMonth: Int = 5,
        doneAt: Long? = if (status == ExpenseStatus.DONE) 1L else null
    ) {
        if (db.monthLedgerDao().getById(ledgerId) == null) {
            db.monthLedgerDao().insert(
                MonthLedgerEntity(ledgerId, ledgerId, createdAtEpochMs = 1L)
            )
        }
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = id,
                ledgerId = ledgerId,
                templateId = "tpl",
                label = "Loyer",
                amountCents = 47_000L,
                dayOfMonth = dayOfMonth,
                status = status,
                doneAtEpochMs = doneAt,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
    }
}
