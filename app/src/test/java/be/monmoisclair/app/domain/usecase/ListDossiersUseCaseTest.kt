package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class ListDossiersUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var list: ListDossiersUseCase
    private val brussels: ZoneId = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        list = ListDossiersUseCase(db.templateDao(), db.instanceDao())
        val instant = java.time.OffsetDateTime.parse("2026-10-15T12:00:00+02:00").toInstant()
        AppClock.clock = Clock.fixed(instant, brussels)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    @Test
    fun empty_whenNoDossiers() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-rent",
                label = "Loyer",
                amountCents = 47_000L,
                dayOfMonth = 1,
                active = true,
                openingRemainingCents = null,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        val snap = list()
        assertThat(snap.count).isEqualTo(0)
        assertThat(snap.monthTotalCents).isEqualTo(0L)
    }

    @Test
    fun listsOpenDossier_withRemainingAndMonthAmount() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-acc",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 12,
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
                id = "exp:2026-10:tpl-acc",
                ledgerId = "2026-10",
                templateId = "tpl-acc",
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
        val snap = list()
        assertThat(snap.count).isEqualTo(1)
        assertThat(snap.monthTotalCents).isEqualTo(5_000L)
        val item = snap.items.single()
        assertThat(item.who).isEqualTo("Étude")
        assertThat(item.what).isEqualTo("Hôpital")
        assertThat(item.remainingCents).isEqualTo(20_000L)
        assertThat(item.isSettled).isFalse()
        assertThat(item.openInstanceId).isEqualTo("exp:2026-10:tpl-acc")
        assertThat(item.dueThisMonth).isTrue()
        assertThat(item.cadenceLabel).isEqualTo("50 € / mois")
        assertThat(item.pacePhrase)
            .isEqualTo("Encore environ 4 mois si le versement reste de 50 €.")
    }

    @Test
    fun offMonthDossier_doesNotCountInThisMonthTotal() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-tax",
                label = "Taxe",
                amountCents = 12_000L,
                dayOfMonth = 3,
                active = true,
                intervalMonths = 12,
                causeLabel = "2025",
                openingRemainingCents = 12_000L,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        val snap = list()
        assertThat(snap.count).isEqualTo(1)
        assertThat(snap.monthTotalCents).isEqualTo(0L)
        val item = snap.items.single()
        assertThat(item.monthCents).isEqualTo(12_000L)
        assertThat(item.dueThisMonth).isFalse()
        assertThat(item.cadenceLabel).isEqualTo("120 € / an")
        assertThat(item.pacePhrase)
            .isEqualTo("Encore environ 1 versement si le versement reste de 120 €.")
    }
}
