package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.MonthCycleEngine
import be.monmoisclair.app.domain.model.AppMetaKeys
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
import be.monmoisclair.app.data.widget.CarnetWidgetSink

@RunWith(RobolectricTestRunner::class)
class ConfigureSavingsUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var configure: ConfigureSavingsUseCase
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
            },
            widgetSink = CarnetWidgetSink.Noop
        )
        val engine = MonthCycleEngine(db.monthLedgerDao(), repo)
        add = AddEntryUseCase(engine, repo)
        configure = ConfigureSavingsUseCase(
            context,
            addEntry = add,
            mutatingRepository = repo,
            instanceDao = db.instanceDao(),
            templateDao = db.templateDao(),
            appMetaDao = db.appMetaDao(),
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) = Unit
            }
        )
        AppClock.clock = Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), brussels)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    private suspend fun seedMonthWithIncomeAndRent() {
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L))
        db.instanceDao().insertIncome(
            IncomeInstanceEntity(
                id = "inc-1",
                ledgerId = "2026-10",
                templateId = null,
                label = "Salaire",
                amountCents = 100_000L,
                dayOfMonth = 1,
                createdAtEpochMs = 1L
            )
        )
        add(
            label = "Loyer",
            amountCents = 80_000L,
            dayOfMonth = 5,
            isExpense = true,
            recurring = true
        )
    }

    @Test
    fun save_createsMonthlyLine_inRemainingAndStillToPay() = runBlocking {
        seedMonthWithIncomeAndRent()
        // Reste avant épargne : 1000 − 800 = 200
        val before = configure.previewAmount(5_000L)
        assertThat(before.remainingCents).isEqualTo(15_000L)
        assertThat(before.alreadyTransferredThisMonth).isFalse()

        val result = configure.save(amountCents = 5_000L, dayOfMonth = 10)
        assertThat(result).isEqualTo(ConfigureSavingsResult.Saved)

        val state = configure.read()
        assertThat(state.active).isTrue()
        assertThat(state.amountCents).isEqualTo(5_000L)
        assertThat(state.dayOfMonth).isEqualTo(10)
        assertThat(state.transferredCents).isEqualTo(0L)

        val meta = db.appMetaDao().get(AppMetaKeys.SAVINGS_TEMPLATE_ID)?.value
        assertThat(meta).isNotEmpty()

        val expenses = db.instanceDao().getAllExpenses().filter { it.ledgerId == "2026-10" }
        val savings = expenses.single { it.label == ConfigureSavingsUseCase.LABEL }
        assertThat(savings.amountCents).isEqualTo(5_000L)
        assertThat(savings.status).isEqualTo(ExpenseStatus.TODO)

        // 1000 − 800 − 50 = 150 encore à payer tant que pas FAIT
        val remaining = configure.previewAmount(5_000L)
        assertThat(remaining.remainingCents).isEqualTo(15_000L)
    }

    @Test
    fun projected_announcesDeficitWhenTooHigh() = runBlocking {
        seedMonthWithIncomeAndRent()
        // 1000 − 800 = 200 ; 250 → déficit 50
        assertThat(configure.previewAmount(25_000L).remainingCents).isEqualTo(-5_000L)
    }

    @Test
    fun remove_archivesTemplate_andClearsMeta() = runBlocking {
        seedMonthWithIncomeAndRent()
        configure.save(5_000L, 10)
        assertThat(configure.remove()).isEqualTo(ConfigureSavingsResult.Removed)
        assertThat(configure.read().active).isFalse()
        assertThat(db.appMetaDao().get(AppMetaKeys.SAVINGS_TEMPLATE_ID)?.value.orEmpty())
            .isEmpty()
        val todos = db.instanceDao().getAllExpenses()
            .filter { it.label == ConfigureSavingsUseCase.LABEL && it.status == ExpenseStatus.TODO }
        assertThat(todos).isEmpty()
    }

    @Test
    fun save_rejectsNonPositive() = runBlocking {
        val r = configure.save(0L, 1)
        assertThat(r).isInstanceOf(ConfigureSavingsResult.InvalidInput::class.java)
    }

    @Test
    fun save_ignoresBrokenFutureLedgerIds() = runBlocking {
        seedMonthWithIncomeAndRent()
        assertThat(configure.save(5_000L, 10)).isEqualTo(ConfigureSavingsResult.Saved)
        val templateId = db.appMetaDao().get(AppMetaKeys.SAVINGS_TEMPLATE_ID)?.value.orEmpty()
        db.monthLedgerDao().insert(
            MonthLedgerEntity(
                id = "bad-ledger",
                yearMonth = "bad-ledger",
                createdAtEpochMs = 1L
            )
        )
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp-bad-ledger",
                ledgerId = "bad-ledger",
                templateId = templateId,
                label = ConfigureSavingsUseCase.LABEL,
                amountCents = 5_000L,
                dayOfMonth = 10,
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                isAutoDebit = false,
                paymentNote = "",
                receiptRelativePath = "",
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )

        val result = configure.save(6_000L, 12)

        assertThat(result).isEqualTo(ConfigureSavingsResult.Saved)
        val current = db.instanceDao().getAllExpenses()
            .single { it.templateId == templateId && it.ledgerId == "2026-10" }
        assertThat(current.amountCents).isEqualTo(6_000L)
        assertThat(current.dayOfMonth).isEqualTo(12)
    }

    @Test
    fun createLineFailed_copy_staysPlain() {
        assertThat(ConfigureSavingsUseCase.CREATE_LINE_FAILED.lowercase())
            .doesNotContain("réessayez")
        assertThat(ConfigureSavingsUseCase.CREATE_LINE_FAILED).contains("créée")
    }
}
