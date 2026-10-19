package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.YearMonth

@RunWith(RobolectricTestRunner::class)
class ListNextMonthPeriodicsUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: ListNextMonthPeriodicsUseCase

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        useCase = ListNextMonthPeriodicsUseCase(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun warns_when_due_next_month() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-ins",
                label = "Assurance",
                amountCents = 450_00L,
                dayOfMonth = 12,
                active = true,
                intervalMonths = 12,
                anchorYearMonth = "2026-11",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        val list = useCase(YearMonth.of(2026, 10))
        assertThat(list).hasSize(1)
        assertThat(list[0].label).isEqualTo("Assurance")
    }

    @Test
    fun empty_when_not_next_month() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-tax",
                label = "Taxe",
                amountCents = 100_00L,
                dayOfMonth = 1,
                active = true,
                intervalMonths = 12,
                anchorYearMonth = "2027-03",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        assertThat(useCase(YearMonth.of(2026, 10))).isEmpty()
    }

    @Test
    fun ignores_monthly_templates() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-rent",
                label = "Loyer",
                amountCents = 900_00L,
                dayOfMonth = 1,
                active = true,
                intervalMonths = 1,
                anchorYearMonth = "2026-01",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        assertThat(useCase(YearMonth.of(2026, 10))).isEmpty()
    }
}
