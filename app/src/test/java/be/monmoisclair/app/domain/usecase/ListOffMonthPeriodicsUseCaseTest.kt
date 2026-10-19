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
}
