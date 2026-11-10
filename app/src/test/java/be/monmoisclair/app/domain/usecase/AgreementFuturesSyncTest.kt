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

/**
 * Complète DossierAgreementUseCaseTest — règles pures [AgreementFuturesSync].
 */
@RunWith(RobolectricTestRunner::class)
class AgreementFuturesSyncTest {

    private lateinit var db: AppDatabase
    private val brussels: ZoneId = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        freezeAt("2026-10-05T12:00:00+02:00")
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    @Test
    fun afterToggle_intermediateDone_keepsFutureTodo() = runBlocking {
        seedDossier(opening = 20_000L, forfait = 5_000L)
        val oct = db.instanceDao().getExpenseById("exp:2026-10:tpl-sync")!!
        val done = oct.copy(status = ExpenseStatus.DONE, doneAtEpochMs = 1L)
        db.instanceDao().updateExpense(done)

        AgreementFuturesSync.afterToggle(
            expense = done,
            instanceDao = db.instanceDao(),
            templateDao = db.templateDao(),
            monthLedgerDao = db.monthLedgerDao()
        )

        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-sync")).isNotNull()
    }

    @Test
    fun afterToggle_settled_deletesFutureTodo() = runBlocking {
        seedDossier(opening = 5_000L, forfait = 5_000L)
        val oct = db.instanceDao().getExpenseById("exp:2026-10:tpl-sync")!!
        val done = oct.copy(status = ExpenseStatus.DONE, doneAtEpochMs = 1L)
        db.instanceDao().updateExpense(done)

        AgreementFuturesSync.afterToggle(
            expense = done,
            instanceDao = db.instanceDao(),
            templateDao = db.templateDao(),
            monthLedgerDao = db.monthLedgerDao()
        )

        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-sync")).isNull()
    }

    private suspend fun seedDossier(opening: Long, forfait: Long) {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-sync",
                label = "Étude",
                amountCents = forfait,
                dayOfMonth = 12,
                active = true,
                anchorYearMonth = "2026-10",
                intervalMonths = 1,
                openingRemainingCents = opening,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L))
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-11", "2026-11", createdAtEpochMs = 1L))
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp:2026-10:tpl-sync",
                ledgerId = "2026-10",
                templateId = "tpl-sync",
                label = "Étude",
                amountCents = forfait,
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
                id = "exp:2026-11:tpl-sync",
                ledgerId = "2026-11",
                templateId = "tpl-sync",
                label = "Étude",
                amountCents = forfait,
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
    }

    private fun freezeAt(isoOffsetDateTime: String) {
        val instant = java.time.OffsetDateTime.parse(isoOffsetDateTime).toInstant()
        AppClock.clock = Clock.fixed(instant, brussels)
    }
}
