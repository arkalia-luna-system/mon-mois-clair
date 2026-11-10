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
import java.time.Instant
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class FindSimilarExpensesUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: FindSimilarExpensesUseCase
    private val brussels = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        useCase = FindSimilarExpensesUseCase(db.instanceDao(), db.templateDao())
        AppClock.clock = Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), brussels)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    private suspend fun ensureLedger(ledgerId: String) {
        db.monthLedgerDao().insert(MonthLedgerEntity(ledgerId, ledgerId, createdAtEpochMs = 1L))
    }

    @Test
    fun findInLedger_returnsMatchSameLedger() = runBlocking {
        val ledgerId = AppClock.ledgerId(AppClock.currentYearMonth())
        ensureLedger(ledgerId)
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp-1",
                ledgerId = ledgerId,
                templateId = null,
                label = "Proximus",
                amountCents = 4_500L,
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
                createdAtEpochMs = 0L
            )
        )
        val hits = useCase.findInLedger(
            ledgerId = ledgerId,
            label = "proximus",
            amountCents = 4_500L,
            dayOfMonth = 10
        )
        assertThat(hits).hasSize(1)
        assertThat(hits[0].existingLabel).isEqualTo("Proximus")
    }

    @Test
    fun findInLedger_usesTemplateCauseForCreditor() = runBlocking {
        val ledgerId = AppClock.ledgerId(AppClock.currentYearMonth())
        ensureLedger(ledgerId)
        val tplId = "tpl-acc"
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = tplId,
                label = "Versement",
                amountCents = 10_000L,
                dayOfMonth = 5,
                active = true,
                remindJ3 = true,
                remindJd = true,
                isAutoDebit = false,
                intervalMonths = 1,
                anchorYearMonth = "2026-10",
                paymentNote = "",
                receiptRelativePath = "",
                causeLabel = "Huissier Martin",
                openingRemainingCents = 50_000L,
                iban = "",
                creditorEmail = "",
                createdAtEpochMs = 0L,
                archivedAtEpochMs = null
            )
        )
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp-1",
                ledgerId = ledgerId,
                templateId = tplId,
                label = "Versement",
                amountCents = 10_000L,
                dayOfMonth = 5,
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                isAutoDebit = false,
                paymentNote = "",
                receiptRelativePath = "",
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 0L
            )
        )
        val hits = useCase.findInLedger(
            ledgerId = ledgerId,
            label = "Huissier Martin",
            amountCents = 10_000L,
            dayOfMonth = 5
        )
        assertThat(hits).hasSize(1)
    }
}
