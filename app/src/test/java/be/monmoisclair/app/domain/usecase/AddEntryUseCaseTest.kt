package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.MonthCycleEngine
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class AddEntryUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var add: AddEntryUseCase
    private val brussels: ZoneId = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = MutatingRepository(
            db = db,
            backupScheduler = BackupScheduler {},
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) = Unit
            }
        )
        val engine = MonthCycleEngine(db.monthLedgerDao(), repo)
        add = AddEntryUseCase(engine, repo)
        AppClock.clock = Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), brussels)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    @Test
    fun add_fromPastLedger_usesDisplayedMonth_notCurrent() = runBlocking {
        val past = YearMonth.of(2026, 3)
        val result = add(
            label = "Loyer mars",
            amountCents = 47_000L,
            dayOfMonth = 5,
            isExpense = true,
            recurring = false,
            targetYearMonth = past
        )
        assertThat(result).isInstanceOf(AddEntryResult.Success::class.java)
        val expenses = db.instanceDao().observeExpenses("2026-03").first()
        assertThat(expenses).hasSize(1)
        assertThat(expenses[0].label).isEqualTo("Loyer mars")
        assertThat(expenses[0].ledgerId).isEqualTo("2026-03")
        assertThat(db.instanceDao().observeExpenses("2026-10").first()).isEmpty()
    }

    @Test
    fun add_rejectsFutureMonth() = runBlocking {
        val future = YearMonth.of(2026, 11)
        val result = add(
            label = "Trop tôt",
            amountCents = 1_000L,
            dayOfMonth = 1,
            isExpense = true,
            recurring = false,
            targetYearMonth = future
        )
        assertThat(result).isInstanceOf(AddEntryResult.InvalidInput::class.java)
    }
}
