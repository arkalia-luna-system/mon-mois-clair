package be.monmoisclair.app.data.reminder

import android.app.NotificationManager
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class ReminderSchedulerImplTest {

    private lateinit var db: AppDatabase
    private lateinit var gateway: RecordingExactAlarmGateway
    private lateinit var scheduler: ReminderSchedulerImpl
    private lateinit var context: Context
    private val brussels = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        gateway = RecordingExactAlarmGateway()
        scheduler = ReminderSchedulerImpl(
            db.instanceDao(),
            gateway,
            db.appMetaDao(),
            PaymentNotificationHelper(context)
        )
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
        assertThat(gateway.cancelled).containsAtLeast(
            ReminderRequestCodes.of("exp-b", ReminderKind.J3),
            ReminderRequestCodes.of("exp-b", ReminderKind.JD),
            ReminderRequestCodes.of("exp-b", ReminderKind.OVERDUE)
        )
    }

    @Test
    fun cancelFor_cancelsAllKinds() = runBlocking<Unit> {
        scheduler.cancelFor("exp-x")
        assertThat(gateway.cancelled).containsExactly(
            ReminderRequestCodes.of("exp-x", ReminderKind.J3),
            ReminderRequestCodes.of("exp-x", ReminderKind.JD),
            ReminderRequestCodes.of("exp-x", ReminderKind.OVERDUE)
        )
    }

    @Test
    @Config(sdk = [32])
    fun cancelFor_clearsVisibleNotificationsToo() = runBlocking<Unit> {
        val manager = context.getSystemService(NotificationManager::class.java)!!
        shadowOf(manager).setNotificationsEnabled(true)
        val helper = PaymentNotificationHelper(context)
        helper.showPaymentReminder(
            notificationId = ReminderRequestCodes.of("exp-visible", ReminderKind.J3),
            title = "Dans 3 jours : Loyer",
            body = "47,00 € — le 5. Pensez à payer.",
            expenseInstanceId = "exp-visible"
        )
        helper.showPaymentReminder(
            notificationId = ReminderRequestCodes.of("exp-visible", ReminderKind.JD),
            title = "Aujourd’hui : Loyer",
            body = "47,00 € — à payer aujourd’hui (le 5).",
            expenseInstanceId = "exp-visible"
        )
        assertThat(shadowOf(manager).activeNotifications.map { it.id }).isNotEmpty()

        scheduler.cancelFor("exp-visible")

        assertThat(shadowOf(manager).activeNotifications.map { it.id }).isEmpty()
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
    fun reconcileAll_schedulesOverdue_whenPastDueDay() = runBlocking<Unit> {
        // Le 16 octobre : échéance le 15 dépassée → OVERDUE quotidien
        val instant = java.time.OffsetDateTime.parse("2026-10-16T12:00:00+02:00").toInstant()
        AppClock.clock = Clock.fixed(instant, brussels)
        seedExpense("exp-over", "2026-10", 15, ExpenseStatus.TODO)

        scheduler.reconcileAll()

        assertThat(gateway.scheduled.map { it.payload.kind }).contains(ReminderKind.OVERDUE)
        assertThat(gateway.scheduled.any { it.payload.title.contains("retard") }).isTrue()
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

    @Test
    fun reconcileAll_continues_whenOneExpenseHasDay31InFebruary() = runBlocking<Unit> {
        // P3-R01 : une ligne 31/02 ne doit pas empêcher les autres rappels.
        seedExpense(
            id = "exp-bad",
            ledgerId = "2025-02",
            day = 31,
            status = ExpenseStatus.TODO
        )
        seedExpense(
            id = "exp-ok",
            ledgerId = "2026-10",
            day = 5,
            status = ExpenseStatus.TODO
        )
        scheduler.reconcileAll()
        assertThat(gateway.scheduled.any { it.payload.expenseInstanceId == "exp-ok" }).isTrue()
        assertThat(gateway.scheduled.any { it.payload.title.startsWith("Dans 3 jours") }).isTrue()
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
            db.monthLedgerDao().insert(MonthLedgerEntity(ledgerId, ledgerId, createdAtEpochMs = 1L))
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
