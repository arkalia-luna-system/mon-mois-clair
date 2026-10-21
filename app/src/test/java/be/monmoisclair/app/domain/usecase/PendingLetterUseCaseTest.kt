package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.DossierRemaining
import be.monmoisclair.app.domain.MonthCycleEngine
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
class PendingLetterUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var mutating: MutatingRepository
    private lateinit var create: CreatePendingLetterUseCase
    private lateinit var list: ListPendingLettersUseCase
    private lateinit var convert: ConvertPendingLetterToDossierUseCase
    private lateinit var delete: DeletePendingLetterUseCase
    private val brussels: ZoneId = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        mutating = MutatingRepository(
            db = db,
            backupScheduler = BackupScheduler {},
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) = Unit
            }
        )
        create = CreatePendingLetterUseCase(mutating)
        list = ListPendingLettersUseCase(db.pendingLetterDao())
        val engine = MonthCycleEngine(db.monthLedgerDao(), mutating)
        convert = ConvertPendingLetterToDossierUseCase(
            db.pendingLetterDao(),
            engine,
            mutating
        )
        delete = DeletePendingLetterUseCase(mutating)
        AppClock.clock = Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), brussels)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    @Test
    fun create_requiresWho() = runBlocking {
        val r = create(who = "  ")
        assertThat(r).isInstanceOf(CreatePendingLetterResult.InvalidInput::class.java)
        assertThat(list()).isEmpty()
    }

    @Test
    fun create_storesWithoutMonthLine() = runBlocking {
        val r = create(who = "Étude", what = "Hôpital", iban = "BE68539007547034")
        assertThat(r).isInstanceOf(CreatePendingLetterResult.Success::class.java)
        val letters = list()
        assertThat(letters).hasSize(1)
        assertThat(letters[0].who).isEqualTo("Étude")
        assertThat(letters[0].what).isEqualTo("Hôpital")
        assertThat(letters[0].iban).isEqualTo("BE68539007547034")
        assertThat(db.instanceDao().getAllExpenses()).isEmpty()
        assertThat(db.templateDao().getAllExpenseTemplates()).isEmpty()
    }

    @Test
    fun convert_createsDossierAndRemovesLetter() = runBlocking {
        val created = create(who = "Étude", what = "Hôpital") as CreatePendingLetterResult.Success
        val result = convert(
            letterId = created.id,
            monthlyAmountCents = 5_000L,
            openingRemainingCents = 60_000L,
            dayOfMonth = 12
        )
        assertThat(result).isInstanceOf(ConvertPendingLetterResult.Success::class.java)
        assertThat(list()).isEmpty()
        val tpls = db.templateDao().getAllExpenseTemplates()
        assertThat(tpls).hasSize(1)
        val tpl = tpls.single()
        assertThat(tpl.label).isEqualTo("Étude")
        assertThat(tpl.causeLabel).isEqualTo("Hôpital")
        assertThat(tpl.openingRemainingCents).isEqualTo(60_000L)
        assertThat(tpl.amountCents).isEqualTo(5_000L)
        assertThat(DossierRemaining.isDossier(tpl.openingRemainingCents)).isTrue()
        val expenses = db.instanceDao().getAllExpenses()
        assertThat(expenses).hasSize(1)
        assertThat(expenses.single().amountCents).isEqualTo(5_000L)
        assertThat(expenses.single().status).isEqualTo(ExpenseStatus.TODO)
    }

    @Test
    fun delete_removesLetter() = runBlocking {
        val created = create(who = "X") as CreatePendingLetterResult.Success
        delete(created.id)
        assertThat(list()).isEmpty()
    }
}
