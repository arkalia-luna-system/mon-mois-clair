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
    private lateinit var repo: MutatingRepository
    private val brussels: ZoneId = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = MutatingRepository(
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

    private fun mutatingRepo(): MutatingRepository = repo

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
    fun toggle_settled_deletesFutureTodos() = runBlocking {
        freezeAt("2026-01-15T10:00:00+01:00")
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-sync",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 1,
                active = true,
                openingRemainingCents = 5_000L,
                anchorYearMonth = "2026-01",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        for (lid in listOf("2026-01", "2026-02", "2026-03")) {
            db.monthLedgerDao().insert(MonthLedgerEntity(lid, lid, createdAtEpochMs = 1L))
            db.instanceDao().insertExpense(
                ExpenseInstanceEntity(
                    id = "exp:$lid:tpl-sync",
                    ledgerId = lid,
                    templateId = "tpl-sync",
                    label = "Étude",
                    amountCents = 5_000L,
                    dayOfMonth = 1,
                    status = ExpenseStatus.TODO,
                    doneAtEpochMs = null,
                    remindJ3 = true,
                    remindJd = true,
                    alarmJ3RequestCode = null,
                    alarmJdRequestCode = null,
                    createdAtEpochMs = 1L
                )
            )
        }
        val toggle = ToggleExpenseDoneUseCase(db.instanceDao(), mutatingRepo())
        toggle("exp:2026-01:tpl-sync")
        assertThat(db.instanceDao().getExpenseById("exp:2026-01:tpl-sync")!!.status)
            .isEqualTo(ExpenseStatus.DONE)
        assertThat(db.instanceDao().getExpenseById("exp:2026-02:tpl-sync")).isNull()
        assertThat(db.instanceDao().getExpenseById("exp:2026-03:tpl-sync")).isNull()
    }

    @Test
    fun toggle_remettre_lastPayment_doesNotRecreateWhenCurrentTodoCoversRemaining() =
        runBlocking {
            freezeAt("2026-01-15T10:00:00+01:00")
            db.templateDao().insertExpense(
                ExpenseTemplateEntity(
                    id = "tpl-rem",
                    label = "Étude",
                    amountCents = 5_000L,
                    dayOfMonth = 1,
                    active = true,
                    openingRemainingCents = 5_000L,
                    anchorYearMonth = "2026-01",
                    createdAtEpochMs = 1L,
                    archivedAtEpochMs = null
                )
            )
            for (lid in listOf("2026-01", "2026-02")) {
                db.monthLedgerDao().insert(MonthLedgerEntity(lid, lid, createdAtEpochMs = 1L))
            }
            db.instanceDao().insertExpense(
                ExpenseInstanceEntity(
                    id = "exp:2026-01:tpl-rem",
                    ledgerId = "2026-01",
                    templateId = "tpl-rem",
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
            val toggle = ToggleExpenseDoneUseCase(db.instanceDao(), mutatingRepo())
            // Remettre le dernier paiement : janvier TODO couvre déjà tout → pas de février.
            toggle("exp:2026-01:tpl-rem")
            assertThat(db.instanceDao().getExpenseById("exp:2026-02:tpl-rem")).isNull()
        }

    @Test
    fun toggle_remettre_splitsRemainingChronologicallyAcrossOpenMonths() = runBlocking {
        freezeAt("2026-01-15T10:00:00+01:00")
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-split",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 1,
                active = true,
                openingRemainingCents = 12_000L,
                anchorYearMonth = "2026-01",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        for (lid in listOf("2026-01", "2026-02", "2026-03", "2026-04")) {
            db.monthLedgerDao().insert(MonthLedgerEntity(lid, lid, createdAtEpochMs = 1L))
        }
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp:2026-01:tpl-split",
                ledgerId = "2026-01",
                templateId = "tpl-split",
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
        val toggle = ToggleExpenseDoneUseCase(db.instanceDao(), mutatingRepo())
        // Remettre janv. : reste 12 000 − 5 000 (janv. TODO) = 7 000
        // → fév. 5 000, mars 2 000, avril rien.
        toggle("exp:2026-01:tpl-split")
        assertThat(db.instanceDao().getExpenseById("exp:2026-02:tpl-split")!!.amountCents)
            .isEqualTo(5_000L)
        assertThat(db.instanceDao().getExpenseById("exp:2026-03:tpl-split")!!.amountCents)
            .isEqualTo(2_000L)
        assertThat(db.instanceDao().getExpenseById("exp:2026-04:tpl-split")).isNull()
    }

    @Test
    fun update_clearsCauseLabel_whenEmptyStringSent() = runBlocking {
        freezeAt("2026-10-05T12:00:00+02:00")
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-cause",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 5,
                active = true,
                causeLabel = "Hôpital",
                openingRemainingCents = 20_000L,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L))
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp:2026-10:tpl-cause",
                ledgerId = "2026-10",
                templateId = "tpl-cause",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 5,
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        val update = UpdateInstanceUseCase(db.instanceDao(), mutatingRepo())
        val result = update(
            id = "exp:2026-10:tpl-cause",
            isExpense = true,
            label = "Étude",
            amountCents = 5_000L,
            dayOfMonth = 5,
            applyToFollowingMonths = false,
            causeLabel = ""
        )
        assertThat(result).isEqualTo(UpdateInstanceResult.Success)
        assertThat(db.templateDao().getExpenseById("tpl-cause")!!.causeLabel).isEmpty()
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
    fun openedNextMonth_dropsWhenThisPaymentFinishesTheDebt() = runBlocking {
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
        // Remettre le dernier paiement : octobre TODO couvre le reste → pas de novembre.
        toggle("exp:2026-10:tpl-stop")
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-stop")).isNull()
    }

    private fun freezeAt(isoOffsetDateTime: String) {
        val instant = java.time.OffsetDateTime.parse(isoOffsetDateTime).toInstant()
        AppClock.clock = Clock.fixed(instant, brussels)
    }
}
