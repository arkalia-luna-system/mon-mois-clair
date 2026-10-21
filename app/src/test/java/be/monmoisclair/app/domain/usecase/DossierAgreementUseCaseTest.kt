package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.MonthCycleEngine
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.YearMonth
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class DossierAgreementUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var add: AddEntryUseCase
    private lateinit var engine: MonthCycleEngine
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
        engine = MonthCycleEngine(db.monthLedgerDao(), repo)
        add = AddEntryUseCase(engine, repo)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    @Test
    fun addWithoutDossierBlock_leavesOpeningNull_homeLooksNormal() = runBlocking {
        freezeAt("2026-10-05T12:00:00+02:00")
        val result = add(
            label = "Loyer",
            amountCents = 47_000L,
            dayOfMonth = 5,
            isExpense = true,
            recurring = true
        )
        assertThat(result).isInstanceOf(AddEntryResult.Success::class.java)
        val tpl = db.templateDao().getAllExpenseTemplates().single()
        assertThat(tpl.openingRemainingCents).isNull()
        assertThat(tpl.causeLabel).isEmpty()
    }

    @Test
    fun remaining_usesActualDoneCents_notTemplateAmount() = runBlocking {
        freezeAt("2026-10-05T12:00:00+02:00")
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-acc",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 12,
                active = true,
                causeLabel = "Hôpital",
                openingRemainingCents = 60_000L,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L))
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp:2026-10:tpl-acc",
                ledgerId = "2026-10",
                templateId = "tpl-acc",
                label = "Étude",
                amountCents = 3_000L,
                dayOfMonth = 12,
                status = ExpenseStatus.DONE,
                doneAtEpochMs = 2L,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        val paid = db.instanceDao().sumAmountCentsByTemplateAndStatus(
            "tpl-acc",
            ExpenseStatus.DONE
        )
        assertThat(paid).isEqualTo(3_000L)
        assertThat(DossierRemaining.remainingCents(60_000L, paid)).isEqualTo(57_000L)
    }

    @Test
    fun twelfthMonth_settles_andStopsNextMonth() = runBlocking {
        freezeAt("2026-12-01T10:00:00+01:00")
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-12",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 1,
                active = true,
                openingRemainingCents = 60_000L,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        // 11 mois déjà FAIT à 5 000 = 55 000 ; décembre sera le 12ᵉ.
        for (month in 1..11) {
            val lid = "2026-%02d".format(month)
            db.monthLedgerDao().insert(MonthLedgerEntity(lid, lid, createdAtEpochMs = 1L))
            db.instanceDao().insertExpense(
                ExpenseInstanceEntity(
                    id = "exp:$lid:tpl-12",
                    ledgerId = lid,
                    templateId = "tpl-12",
                    label = "Étude",
                    amountCents = 5_000L,
                    dayOfMonth = 1,
                    status = ExpenseStatus.DONE,
                    doneAtEpochMs = 2L,
                    remindJ3 = true,
                    remindJd = true,
                    alarmJ3RequestCode = null,
                    alarmJdRequestCode = null,
                    createdAtEpochMs = 1L
                )
            )
        }
        engine.ensureMonthExists(YearMonth.of(2026, 12))
        val dec = db.instanceDao().observeExpenses("2026-12").first().single()
        assertThat(dec.amountCents).isEqualTo(5_000L)
        db.instanceDao().updateExpense(
            dec.copy(status = ExpenseStatus.DONE, doneAtEpochMs = 3L)
        )

        freezeAt("2027-01-01T10:00:00+01:00")
        engine.ensureMonthExists(YearMonth.of(2027, 1))
        assertThat(db.instanceDao().observeExpenses("2027-01").first()).isEmpty()
    }

    @Test
    fun lastMonth_usesRemainder_notFullInstallment() = runBlocking {
        freezeAt("2026-11-01T10:00:00+01:00")
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-part",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 4,
                active = true,
                anchorYearMonth = "2026-01",
                openingRemainingCents = 53_000L,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        for (month in 1..10) {
            val lid = "2026-%02d".format(month)
            db.monthLedgerDao().insert(MonthLedgerEntity(lid, lid, createdAtEpochMs = 1L))
            db.instanceDao().insertExpense(
                ExpenseInstanceEntity(
                    id = "exp:$lid:tpl-part",
                    ledgerId = lid,
                    templateId = "tpl-part",
                    label = "Étude",
                    amountCents = 5_000L,
                    dayOfMonth = 4,
                    status = ExpenseStatus.DONE,
                    doneAtEpochMs = 2L,
                    remindJ3 = true,
                    remindJd = true,
                    alarmJ3RequestCode = null,
                    alarmJdRequestCode = null,
                    createdAtEpochMs = 1L
                )
            )
        }
        engine.ensureMonthExists(YearMonth.of(2026, 11))
        val nov = db.instanceDao().observeExpenses("2026-11").first().single()
        assertThat(nov.amountCents).isEqualTo(3_000L)
    }

    @Test
    fun openedNextMonth_dropsWhenSettled_andReturnsOnUndo() = runBlocking {
        freezeAt("2026-10-05T12:00:00+02:00")
        val repo = MutatingRepository(
            db = db,
            backupScheduler = BackupScheduler {},
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) = Unit
            }
        )
        val toggle = ToggleExpenseDoneUseCase(db.instanceDao(), repo)
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-stop",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 12,
                active = true,
                anchorYearMonth = "2026-10",
                openingRemainingCents = 5_000L,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L))
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-11", "2026-11", createdAtEpochMs = 1L))
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp:2026-10:tpl-stop",
                ledgerId = "2026-10",
                templateId = "tpl-stop",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 12,
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp:2026-11:tpl-stop",
                ledgerId = "2026-11",
                templateId = "tpl-stop",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 12,
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        toggle("exp:2026-10:tpl-stop")
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-stop")).isNull()
        toggle("exp:2026-10:tpl-stop")
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-stop")).isNull()
    }

    private fun freezeAt(isoOffsetDateTime: String) {
        val instant = java.time.OffsetDateTime.parse(isoOffsetDateTime).toInstant()
        AppClock.clock = Clock.fixed(instant, brussels)
    }
}
