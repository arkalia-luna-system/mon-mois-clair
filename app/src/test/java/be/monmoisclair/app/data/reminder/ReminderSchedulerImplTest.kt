package be.monmoisclair.app.data.reminder

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.model.ExpenseStatus
import be.monmoisclair.app.domain.model.ReminderKind
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class ReminderSchedulerImplTest {

    private lateinit var db: AppDatabase
    private lateinit var gateway: RecordingExactAlarmGateway
    private lateinit var scheduler: ReminderSchedulerImpl
    private val brussels = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        gateway = RecordingExactAlarmGateway()
        scheduler = ReminderSchedulerImpl(db.instanceDao(), gateway)
        // 1er octobre 2026 → J−3 et Jour J du 5 encore futurs
        val instant = java.time.OffsetDateTime.parse("2026-10-01T12:00:00+02:00").toInstant()
        AppClock.clock = Clock.fixed(instant, brussels)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    @Test
    fun reconcileAll_schedulesJ3AndJd_forTodo() = runBlocking<Unit> {
        seedExpense("exp-a", "2026-10", 5, ExpenseStatus.TODO)

        scheduler.reconcileAll()

        assertThat(gateway.scheduled).hasSize(2)
        val kinds = gateway.scheduled.map { it.payload.kind }.toSet()
        assertThat(kinds).containsExactly(ReminderKind.J3, ReminderKind.JD)
        assertThat(gateway.scheduled.map { it.requestCode }.toSet()).containsExactly(
            ReminderRequestCodes.of("exp-a", ReminderKind.J3),
            ReminderRequestCodes.of("exp-a", ReminderKind.JD)
        )
    }

    @Test
    fun reconcileAll_cancels_whenDone() = runBlocking<Unit> {
        seedExpense("exp-b", "2026-10", 5, ExpenseStatus.TODO)
        scheduler.reconcileAll()
        gateway.clear()

        db.instanceDao().updateExpense(
            db.instanceDao().getExpenseById("exp-b")!!.copy(
                status = ExpenseStatus.DONE,
                doneAtEpochMs = 1L
            )
        )
        scheduler.reconcileAll()

        assertThat(gateway.scheduled).isEmpty()
        assertThat(gateway.cancelled).containsExactly(
            ReminderRequestCodes.of("exp-b", ReminderKind.J3),
            ReminderRequestCodes.of("exp-b", ReminderKind.JD)
        )
    }

    @Test
    fun cancelFor_cancelsBothKinds() = runBlocking<Unit> {
        scheduler.cancelFor("exp-x")
        assertThat(gateway.cancelled).containsExactly(
            ReminderRequestCodes.of("exp-x", ReminderKind.J3),
            ReminderRequestCodes.of("exp-x", ReminderKind.JD)
        )
    }

    @Test
    fun reconcileAll_skipsPastTriggers_onlySchedulesFuture() = runBlocking<Unit> {
        // Le 4 octobre : J−3 du 5 passé, Jour J encore futur
        val instant = java.time.OffsetDateTime.parse("2026-10-04T12:00:00+02:00").toInstant()
        AppClock.clock = Clock.fixed(instant, brussels)
        seedExpense("exp-c", "2026-10", 5, ExpenseStatus.TODO)

        scheduler.reconcileAll()

        assertThat(gateway.scheduled).hasSize(1)
        assertThat(gateway.scheduled.single().payload.kind).isEqualTo(ReminderKind.JD)
        assertThat(gateway.cancelled).contains(
            ReminderRequestCodes.of("exp-c", ReminderKind.J3)
        )
    }

    @Test
    fun reconcileAll_respectsRemindFlags() = runBlocking<Unit> {
        seedExpense(
            id = "exp-d",
            ledgerId = "2026-10",
            day = 20,
            status = ExpenseStatus.TODO,
            remindJ3 = false,
            remindJd = true
        )
        scheduler.reconcileAll()
        assertThat(gateway.scheduled).hasSize(1)
        assertThat(gateway.scheduled.single().payload.kind).isEqualTo(ReminderKind.JD)
    }

    @Test
    fun requestCodes_areStable() {
        val a = ReminderRequestCodes.of("same", ReminderKind.J3)
        val b = ReminderRequestCodes.of("same", ReminderKind.J3)
        val c = ReminderRequestCodes.of("same", ReminderKind.JD)
        assertThat(a).isEqualTo(b)
        assertThat(a).isNotEqualTo(c)
        assertThat(a).isAtLeast(0)
    }

    private suspend fun seedExpense(
        id: String,
        ledgerId: String,
        day: Int,
        status: ExpenseStatus,
        remindJ3: Boolean = true,
        remindJd: Boolean = true
    ) {
        if (db.monthLedgerDao().getById(ledgerId) == null) {
            db.monthLedgerDao().insert(MonthLedgerEntity(ledgerId, ledgerId, 1L))
        }
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = id,
                ledgerId = ledgerId,
                templateId = "tpl",
                label = "Loyer",
                amountCents = 47_000L,
                dayOfMonth = day,
                status = status,
                doneAtEpochMs = if (status == ExpenseStatus.DONE) 1L else null,
                remindJ3 = remindJ3,
                remindJd = remindJd,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
    }

    private class RecordingExactAlarmGateway : ExactAlarmGateway {
        data class Scheduled(
            val requestCode: Int,
            val triggerAtEpochMs: Long,
            val payload: ReminderPayload
        )

        val scheduled = mutableListOf<Scheduled>()
        val cancelled = mutableListOf<Int>()

        override fun canScheduleExactAlarms(): Boolean = true

        override fun schedule(requestCode: Int, triggerAtEpochMs: Long, payload: ReminderPayload) {
            scheduled += Scheduled(requestCode, triggerAtEpochMs, payload)
        }

        override fun cancel(requestCode: Int) {
            cancelled += requestCode
        }

        fun clear() {
            scheduled.clear()
            cancelled.clear()
        }
    }
}
