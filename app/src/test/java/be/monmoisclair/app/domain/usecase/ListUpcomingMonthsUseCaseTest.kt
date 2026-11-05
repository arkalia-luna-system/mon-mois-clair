package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.IncomeTemplateEntity
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
class ListUpcomingMonthsUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: ListUpcomingMonthsUseCase

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        useCase = ListUpcomingMonthsUseCase(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun incomes_appear_each_preview_month() = runBlocking {
        db.templateDao().insertIncome(
            IncomeTemplateEntity(
                id = "inc-salaire",
                label = "Salaire",
                amountCents = 2_000_00L,
                dayOfMonth = 28,
                active = true,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        val months = useCase(from = YearMonth.of(2026, 10), horizonMonths = 2)
        assertThat(months).hasSize(2)
        assertThat(months[0].yearMonth).isEqualTo(YearMonth.of(2026, 11))
        assertThat(months[0].lines).hasSize(1)
        assertThat(months[0].lines[0].isIncome).isTrue()
        assertThat(months[0].lines[0].cadenceHint).isEqualTo("chaque mois")
        assertThat(months[1].yearMonth).isEqualTo(YearMonth.of(2026, 12))
        assertThat(months[1].lines[0].label).isEqualTo("Salaire")
    }

    @Test
    fun annual_expense_only_on_anchor_month() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-tax",
                label = "Taxe",
                amountCents = 150_00L,
                dayOfMonth = 5,
                active = true,
                intervalMonths = 12,
                anchorYearMonth = "2027-03",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        val months = useCase(from = YearMonth.of(2026, 10), horizonMonths = 6)
        val march = months.firstOrNull { it.yearMonth == YearMonth.of(2027, 3) }
        assertThat(march).isNotNull()
        assertThat(march!!.lines.map { it.label }).contains("Taxe")
        val nov = months.firstOrNull { it.yearMonth == YearMonth.of(2026, 11) }
        assertThat(nov?.lines?.map { it.label }.orEmpty()).doesNotContain("Taxe")
        val tax = march.lines.single()
        assertThat(tax.cadenceHint).isEqualTo("1× par an")
        assertThat(tax.isAgreement).isFalse()
    }

    @Test
    fun settled_dossier_is_omitted() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-dossier",
                label = "Huissier",
                amountCents = 50_00L,
                dayOfMonth = 10,
                active = true,
                intervalMonths = 1,
                anchorYearMonth = "2026-01",
                openingRemainingCents = 50_00L,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.monthLedgerDao().insert(
            MonthLedgerEntity("2026-09", "2026-09", createdAtEpochMs = 1L)
        )
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp-done",
                ledgerId = "2026-09",
                templateId = "tpl-dossier",
                label = "Huissier",
                amountCents = 50_00L,
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
        val months = useCase(from = YearMonth.of(2026, 10), horizonMonths = 2)
        assertThat(months.flatMap { it.lines }.none { it.label == "Huissier" }).isTrue()
    }

    @Test
    fun dossier_preview_reservesCurrentTodo_andSequencesHorizon() = runBlocking {
        // Ouverture 12 000 ; FAIT 5 000 ; courant TODO 5 000 → reste poche 2 000.
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-b8",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 10,
                active = true,
                intervalMonths = 1,
                anchorYearMonth = "2026-01",
                openingRemainingCents = 12_000L,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-01", "2026-01", createdAtEpochMs = 1L))
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-02", "2026-02", createdAtEpochMs = 1L))
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp:2026-01:tpl-b8",
                ledgerId = "2026-01",
                templateId = "tpl-b8",
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
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp:2026-02:tpl-b8",
                ledgerId = "2026-02",
                templateId = "tpl-b8",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 10,
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        val months = useCase(from = YearMonth.of(2026, 2), horizonMonths = 3)
        val etude = months.flatMap { m -> m.lines.map { m.yearMonth to it } }
            .filter { it.second.label == "Étude" }
        assertThat(etude).hasSize(1)
        assertThat(etude[0].first).isEqualTo(YearMonth.of(2026, 3))
        assertThat(etude[0].second.amountCents).isEqualTo(2_000L)
        assertThat(etude[0].second.isAgreement).isTrue()
    }

    @Test
    fun horizon_is_capped_at_six() = runBlocking {
        db.templateDao().insertIncome(
            IncomeTemplateEntity(
                id = "inc",
                label = "Alloc",
                amountCents = 100_00L,
                dayOfMonth = 1,
                active = true,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        val months = useCase(from = YearMonth.of(2026, 1), horizonMonths = 99)
        assertThat(months).hasSize(6)
        assertThat(months.last().yearMonth).isEqualTo(YearMonth.of(2026, 7))
    }
}
