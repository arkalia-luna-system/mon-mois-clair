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
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class AdjustDiversUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var adjust: AdjustDiversUseCase
    private val brussels = ZoneId.of("Europe/Brussels")

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
        adjust = AdjustDiversUseCase(engine, repo)
        AppClock.clock = Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), brussels)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    @Test
    fun add_thenSubtract_clampsAtZero() = runBlocking {
        val ledger = "2026-10"
        assertThat(adjust(ledger, 1_250L)).isEqualTo(AdjustDiversResult.Success(1_250L))
        assertThat(adjust(ledger, 500L)).isEqualTo(AdjustDiversResult.Success(1_750L))
        assertThat(adjust(ledger, -2_000L)).isEqualTo(AdjustDiversResult.Success(0L))
        assertThat(db.monthLedgerDao().getById(ledger)!!.diversSpentCents).isEqualTo(0L)
    }

    @Test
    fun zeroDelta_isRejected() = runBlocking {
        assertThat(adjust("2026-10", 0L)).isInstanceOf(AdjustDiversResult.Failed::class.java)
    }

    @Test
    fun invalidLedgerId_isRejected() = runBlocking {
        assertThat(adjust("pas-un-mois", 100L))
            .isInstanceOf(AdjustDiversResult.Failed::class.java)
    }
}
