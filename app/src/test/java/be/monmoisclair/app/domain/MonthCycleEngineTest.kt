package be.monmoisclair.app.domain

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.IncomeTemplateEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
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
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class MonthCycleEngineTest {

    private lateinit var db: AppDatabase
    private lateinit var engine: MonthCycleEngine
    private val backupCalls = AtomicInteger(0)
    private val reconcileCalls = AtomicInteger(0)
    private val brussels: ZoneId = ZoneId.of("Europe/Brussels")

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
        engine = MonthCycleEngine(
            monthLedgerDao = db.monthLedgerDao(),
            mutatingRepository = repo
        )
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    @Test
    fun ensureCurrentMonth_createsLedgerAndClampedInstances() = runBlocking {
        // 15 février 2025 (non bissextile) — day 31 → 28
        freezeAt("2025-02-15T12:00:00+01:00")
        seedTemplates(
            incomeDay = 1,
            expenseDay = 31,
            inactiveExpense = true
        )

        val result = engine.ensureCurrentMonthExists()
        assertThat(result).isInstanceOf(EnsureMonthResult.Created::class.java)
        val created = result as EnsureMonthResult.Created
        assertThat(created.ledgerId).isEqualTo("2025-02")
        assertThat(created.incomeCount).isEqualTo(1)
        assertThat(created.expenseCount).isEqualTo(1) // inactif exclu

        val expenses = db.instanceDao().observeExpenses("2025-02").first()
        assertThat(expenses).hasSize(1)
        assertThat(expenses[0].dayOfMonth).isEqualTo(28)
        assertThat(expenses[0].label).isEqualTo("Loyer")
        assertThat(expenses[0].amountCents).isEqualTo(47_000L)
        assertThat(expenses[0].status).isEqualTo(ExpenseStatus.TODO)
        assertThat(expenses[0].templateId).isEqualTo("tpl-loyer")

        val incomes = db.instanceDao().observeIncomes("2025-02").first()
        assertThat(incomes).hasSize(1)
        assertThat(incomes[0].dayOfMonth).isEqualTo(1)

        assertThat(backupCalls.get()).isEqualTo(1)
        assertThat(reconcileCalls.get()).isEqualTo(1)
    }

    @Test
    fun ensureCurrentMonth_secondCall_isIdempotent_noDuplicates() = runBlocking {
        freezeAt("2026-10-03T09:00:00+02:00")
        seedTemplates(incomeDay = 5, expenseDay = 5, inactiveExpense = false)

        val first = engine.ensureCurrentMonthExists()
        assertThat(first).isInstanceOf(EnsureMonthResult.Created::class.java)

        val second = engine.ensureCurrentMonthExists()
        assertThat(second).isEqualTo(EnsureMonthResult.AlreadyExists("2026-10"))

        assertThat(db.instanceDao().observeExpenses("2026-10").first()).hasSize(1)
        assertThat(db.instanceDao().observeIncomes("2026-10").first()).hasSize(1)
        // Un seul mutate « create » a déclenché backup/reconcile
        assertThat(backupCalls.get()).isEqualTo(1)
        assertThat(reconcileCalls.get()).isEqualTo(1)
    }

    @Test
    fun ensureMonth_doesNotRegenerateWhenTemplatesChangeLater() = runBlocking {
        freezeAt("2026-04-10T10:00:00+02:00")
        seedTemplates(incomeDay = 1, expenseDay = 10, inactiveExpense = false)

        engine.ensureMonthExists(YearMonth.of(2026, 4))
        assertThat(db.instanceDao().observeExpenses("2026-04").first().single().amountCents)
            .isEqualTo(47_000L)

        // Modification du template après génération du mois
        val updated = db.templateDao().getExpenseById("tpl-loyer")!!.copy(amountCents = 50_000L)
        db.templateDao().updateExpense(updated)

        val again = engine.ensureMonthExists(YearMonth.of(2026, 4))
        assertThat(again).isInstanceOf(EnsureMonthResult.AlreadyExists::class.java)
        assertThat(db.instanceDao().observeExpenses("2026-04").first().single().amountCents)
            .isEqualTo(47_000L) // historique figé
    }

    @Test
    fun ensureMonth_april_clampsDay31To30() = runBlocking {
        freezeAt("2026-04-01T08:00:00+02:00")
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-31",
                label = "Charges",
                amountCents = 10_000L,
                dayOfMonth = 31,
                active = true,
                remindJ3 = true,
                remindJd = true,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )

        engine.ensureMonthExists(YearMonth.of(2026, 4))
        assertThat(db.instanceDao().observeExpenses("2026-04").first().single().dayOfMonth)
            .isEqualTo(30)
    }

    @Test
    fun ensureMonth_leapFebruary_clampsDay31To29() = runBlocking {
        freezeAt("2024-02-01T08:00:00+01:00")
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-31",
                label = "Charges",
                amountCents = 10_000L,
                dayOfMonth = 31,
                active = true,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )

        engine.ensureMonthExists(YearMonth.of(2024, 2))
        assertThat(db.instanceDao().observeExpenses("2024-02").first().single().dayOfMonth)
            .isEqualTo(29)
    }

    @Test
    fun ensureCurrentMonth_withoutTemplates_createsEmptyLedger() = runBlocking {
        freezeAt("2026-06-01T12:00:00+02:00")
        val result = engine.ensureCurrentMonthExists() as EnsureMonthResult.Created
        assertThat(result.incomeCount).isEqualTo(0)
        assertThat(result.expenseCount).isEqualTo(0)
        assertThat(db.monthLedgerDao().getById("2026-06")).isNotNull()
    }

    /** Lot B : dernier mois = reste dû (partiel), pas le forfait plein. */
    @Test
    fun ensureMonth_capsLastInstallmentToRemaining() = runBlocking {
        freezeAt("2026-02-01T10:00:00+01:00")
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-partial",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 10,
                active = true,
                openingRemainingCents = 12_000L,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.monthLedgerDao().insert(
            be.monmoisclair.app.data.db.entity.MonthLedgerEntity(
                id = "2026-01",
                yearMonth = "2026-01",
                createdAtEpochMs = 1L
            )
        )
        db.instanceDao().insertExpense(
            be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity(
                id = "exp:2026-01:tpl-partial",
                ledgerId = "2026-01",
                templateId = "tpl-partial",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 10,
                status = ExpenseStatus.DONE,
                doneAtEpochMs = 2L,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        // Payé 5 000 ; reste 7 000 → février à 5 000 (forfait).
        // On marque encore 5 000 FAIT puis mars doit être 2 000.
        engine.ensureMonthExists(YearMonth.of(2026, 2))
        val feb = db.instanceDao().observeExpenses("2026-02").first().single()
        assertThat(feb.amountCents).isEqualTo(5_000L)
        db.instanceDao().updateExpense(
            feb.copy(status = ExpenseStatus.DONE, doneAtEpochMs = 3L)
        )

        freezeAt("2026-03-01T10:00:00+01:00")
        engine.ensureMonthExists(YearMonth.of(2026, 3))
        val mar = db.instanceDao().observeExpenses("2026-03").first().single()
        assertThat(mar.amountCents).isEqualTo(2_000L)
    }

    /** Lot B : accord soldé → plus de ligne le mois suivant. */
    @Test
    fun ensureMonth_skipsSettledDossier() = runBlocking {
        freezeAt("2026-02-01T10:00:00+01:00")
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-dossier",
                label = "Étude Leroy",
                amountCents = 5_000L,
                dayOfMonth = 10,
                active = true,
                causeLabel = "Hôpital",
                openingRemainingCents = 5_000L,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        // Janvier déjà FAIT pour le montant total → soldé.
        db.monthLedgerDao().insert(
            be.monmoisclair.app.data.db.entity.MonthLedgerEntity(
                id = "2026-01",
                yearMonth = "2026-01",
                createdAtEpochMs = 1L
            )
        )
        db.instanceDao().insertExpense(
            be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity(
                id = "exp:2026-01:tpl-dossier",
                ledgerId = "2026-01",
                templateId = "tpl-dossier",
                label = "Étude Leroy",
                amountCents = 5_000L,
                dayOfMonth = 10,
                status = ExpenseStatus.DONE,
                doneAtEpochMs = 2L,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        engine.ensureMonthExists(YearMonth.of(2026, 2))
        assertThat(db.instanceDao().observeExpenses("2026-02").first()).isEmpty()
    }

    /** Lot A : communication + photo du modèle suivent le mois nouveau. */
    @Test
    fun ensureMonth_copiesPaymentNoteAndReceiptFromTemplate() = runBlocking {
        freezeAt("2026-02-01T10:00:00+01:00")
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-paper",
                label = "Assurance",
                amountCents = 12_000L,
                dayOfMonth = 5,
                active = true,
                paymentNote = "+++000/0000/00097+++",
                receiptRelativePath = "receipts/tpl-paper.webp",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        engine.ensureMonthExists(YearMonth.of(2026, 2))
        val exp = db.instanceDao().observeExpenses("2026-02").first().single()
        assertThat(exp.paymentNote).isEqualTo("+++000/0000/00097+++")
        assertThat(exp.receiptRelativePath).isEqualTo("receipts/tpl-paper.webp")
    }

    private fun freezeAt(isoOffsetDateTime: String) {
        val instant = java.time.OffsetDateTime.parse(isoOffsetDateTime).toInstant()
        AppClock.clock = Clock.fixed(instant, brussels)
    }

    private suspend fun seedTemplates(
        incomeDay: Int,
        expenseDay: Int,
        inactiveExpense: Boolean
    ) {
        db.templateDao().insertIncome(
            IncomeTemplateEntity(
                id = "tpl-salaire",
                label = "Salaire",
                amountCents = 200_000L,
                dayOfMonth = incomeDay,
                active = true,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-loyer",
                label = "Loyer",
                amountCents = 47_000L,
                dayOfMonth = expenseDay,
                active = true,
                remindJ3 = true,
                remindJd = true,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        if (inactiveExpense) {
            db.templateDao().insertExpense(
                ExpenseTemplateEntity(
                    id = "tpl-archive",
                    label = "Ancien",
                    amountCents = 9_999L,
                    dayOfMonth = 15,
                    active = false,
                    createdAtEpochMs = 1L,
                    archivedAtEpochMs = 2L
                )
            )
        }
    }
}
