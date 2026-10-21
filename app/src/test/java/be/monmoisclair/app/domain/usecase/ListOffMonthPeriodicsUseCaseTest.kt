package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.YearMonth

@RunWith(RobolectricTestRunner::class)
class ListOffMonthPeriodicsUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: ListOffMonthPeriodicsUseCase

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        useCase = ListOffMonthPeriodicsUseCase(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun lists_annual_not_due_this_month() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-tax",
                label = "Taxe",
                amountCents = 100_00L,
                dayOfMonth = 15,
                active = true,
                intervalMonths = 12,
                anchorYearMonth = "2027-03",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        val list = useCase(YearMonth.of(2026, 10))
        assertThat(list).hasSize(1)
        assertThat(list[0].label).isEqualTo("Taxe")
        assertThat(list[0].nextDueYearMonth).isEqualTo("2027-03")
    }

    @Test
    fun hides_when_due_this_month() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-water",
                label = "Eau",
                amountCents = 50_00L,
                dayOfMonth = 1,
                active = true,
                intervalMonths = 3,
                anchorYearMonth = "2026-10",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        val list = useCase(YearMonth.of(2026, 10))
        assertThat(list).isEmpty()
    }

    @Test
    fun hides_settled_quarterly_agreement() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-acc-q",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 1,
                active = true,
                intervalMonths = 3,
                anchorYearMonth = "2026-01",
                openingRemainingCents = 5_000L,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-01", "2026-01", createdAtEpochMs = 1L))
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp:2026-01:tpl-acc-q",
                ledgerId = "2026-01",
                templateId = "tpl-acc-q",
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
        // Octobre : trimestriel pas dû ce mois, mais soldé → hors liste.
        val list = useCase(YearMonth.of(2026, 10))
        assertThat(list).isEmpty()
    }
}
