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
import be.monmoisclair.app.data.widget.CarnetWidgetSink
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UpdateExpenseTemplateUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var update: UpdateExpenseTemplateUseCase
    private val brussels = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        AppClock.clock = Clock.fixed(Instant.parse("2026-11-05T10:00:00Z"), brussels)
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
        update = UpdateExpenseTemplateUseCase(repo)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    @Test
    fun update_rewritesFutureTodoInstances() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-tax",
                label = "Taxe",
                amountCents = 25_000L,
                dayOfMonth = 15,
                active = true,
                intervalMonths = 12,
                anchorYearMonth = "2027-03",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.monthLedgerDao().insert(MonthLedgerEntity("2027-03", "2027-03", createdAtEpochMs = 1L))
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp:2027-03:tpl-tax",
                ledgerId = "2027-03",
                templateId = "tpl-tax",
                label = "Taxe",
                amountCents = 25_000L,
                dayOfMonth = 15,
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        assertThat(
            update(
                templateId = "tpl-tax",
                label = "Taxe voiture",
                amountCents = 28_000L,
                dayOfMonth = 20,
                anchorYearMonth = YearMonth.of(2027, 3)
            )
        ).isEqualTo(UpdateExpenseTemplateResult.Success)
        val row = db.instanceDao().getExpenseById("exp:2027-03:tpl-tax")!!
        assertThat(row.label).isEqualTo("Taxe voiture")
        assertThat(row.amountCents).isEqualTo(28_000L)
        assertThat(row.dayOfMonth).isEqualTo(20)
        assertThat(db.templateDao().getExpenseById("tpl-tax")!!.amountCents).isEqualTo(28_000L)
    }
}
