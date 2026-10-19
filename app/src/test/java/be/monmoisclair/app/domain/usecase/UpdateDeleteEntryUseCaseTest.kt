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
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UpdateDeleteEntryUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var update: UpdateInstanceUseCase
    private lateinit var delete: DeleteInstanceUseCase
    private lateinit var add: AddEntryUseCase

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
            }
        )
        update = UpdateInstanceUseCase(db.instanceDao(), repo)
        delete = DeleteInstanceUseCase(db.instanceDao(), repo)
        // AddEntry needs MonthCycleEngine - tested indirectly via seed
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun update_fixesTypoAmount_onCurrentInstanceOnly() = runBlocking {
        seedLinkedExpense(
            ledger = "2026-10",
            id = "exp:2026-10:tpl",
            templateId = "tpl",
            amount = 470_000L
        )
        seedLinkedExpense(
            ledger = "2026-11",
            id = "exp:2026-11:tpl",
            templateId = "tpl",
            amount = 470_000L
        )

        val result = update(
            id = "exp:2026-10:tpl",
            isExpense = true,
            label = "Loyer",
            amountCents = 47_000L,
            dayOfMonth = 5,
            applyToFollowingMonths = false
        )
        assertThat(result).isEqualTo(UpdateInstanceResult.Success)
        assertThat(db.instanceDao().getExpenseById("exp:2026-10:tpl")!!.amountCents)
            .isEqualTo(47_000L)
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl")!!.amountCents)
            .isEqualTo(470_000L)
    }

    @Test
    fun update_applyToFollowing_updatesFutureTodoAndTemplate() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl",
                label = "Loyer",
                amountCents = 470_000L,
                dayOfMonth = 5,
                active = true,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        seedLinkedExpense("2026-10", "exp:2026-10:tpl", "tpl", 470_000L)
        seedLinkedExpense("2026-11", "exp:2026-11:tpl", "tpl", 470_000L)

        update(
            id = "exp:2026-10:tpl",
            isExpense = true,
            label = "Loyer",
            amountCents = 47_000L,
            dayOfMonth = 5,
            applyToFollowingMonths = true
        )

        assertThat(db.instanceDao().getExpenseById("exp:2026-10:tpl")!!.amountCents)
            .isEqualTo(47_000L)
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl")!!.amountCents)
            .isEqualTo(47_000L)
        assertThat(db.templateDao().getExpenseById("tpl")!!.amountCents).isEqualTo(47_000L)
    }

    @Test
    fun delete_removesInstance_andOptionallyFuture() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl2",
                label = "Eau",
                amountCents = 3_000L,
                dayOfMonth = 10,
                active = true,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        seedLinkedExpense("2026-10", "exp:2026-10:tpl2", "tpl2", 3_000L)
        seedLinkedExpense("2026-11", "exp:2026-11:tpl2", "tpl2", 3_000L)

        delete(
            id = "exp:2026-10:tpl2",
            isExpense = true,
            applyToFollowingMonths = true
        )

        assertThat(db.instanceDao().getExpenseById("exp:2026-10:tpl2")).isNull()
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl2")).isNull()
        assertThat(db.templateDao().getExpenseById("tpl2")!!.active).isFalse()
    }

    private suspend fun seedLinkedExpense(
        ledger: String,
        id: String,
        templateId: String,
        amount: Long
    ) {
        if (db.monthLedgerDao().getById(ledger) == null) {
            db.monthLedgerDao().insert(MonthLedgerEntity(ledger, ledger, 1L))
        }
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = id,
                ledgerId = ledger,
                templateId = templateId,
                label = "Loyer",
                amountCents = amount,
                dayOfMonth = 5,
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
}
