package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import be.monmoisclair.app.data.widget.CarnetWidgetSink

@RunWith(RobolectricTestRunner::class)
class ArchiveExpenseTemplateUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var archive: ArchiveExpenseTemplateUseCase

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
            },
            widgetSink = CarnetWidgetSink.Noop
        )
        archive = ArchiveExpenseTemplateUseCase(repo)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun archive_deactivatesTemplate_keepsRow() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-tax",
                label = "Taxe",
                amountCents = 12_000L,
                dayOfMonth = 15,
                active = true,
                intervalMonths = 12,
                anchorYearMonth = "2027-03",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        assertThat(archive("tpl-tax")).isEqualTo(ArchiveExpenseTemplateResult.Success)
        val tpl = db.templateDao().getExpenseById("tpl-tax")!!
        assertThat(tpl.active).isFalse()
        assertThat(tpl.archivedAtEpochMs).isNotNull()
        assertThat(tpl.label).isEqualTo("Taxe")
    }

    @Test
    fun archive_unknown_isNotFound() = runBlocking {
        assertThat(archive("missing")).isEqualTo(ArchiveExpenseTemplateResult.NotFound)
    }
}
