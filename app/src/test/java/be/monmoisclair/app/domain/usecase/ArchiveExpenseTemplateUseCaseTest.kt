package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.reminder.PaymentNotificationHelper
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

    private lateinit var appContext: Context
    private lateinit var db: AppDatabase
    private lateinit var archive: ArchiveExpenseTemplateUseCase

    @Before
    fun setUp() {
        appContext = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java)
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
        archive = ArchiveExpenseTemplateUseCase(
            appContext = appContext,
            mutatingRepository = repo,
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) = Unit
            },
            paymentNotifications = PaymentNotificationHelper(appContext)
        )
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

    @Test
    fun archive_removesFutureTodoInstances() = runBlocking {
        be.monmoisclair.app.domain.AppClock.clock =
            java.time.Clock.fixed(
                java.time.Instant.parse("2026-11-05T10:00:00Z"),
                java.time.ZoneId.of("Europe/Brussels")
            )
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-future",
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
        db.monthLedgerDao().insert(
            be.monmoisclair.app.data.db.entity.MonthLedgerEntity(
                "2027-03",
                "2027-03",
                createdAtEpochMs = 1L
            )
        )
        db.instanceDao().insertExpense(
            be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity(
                id = "exp:2027-03:tpl-future",
                ledgerId = "2027-03",
                templateId = "tpl-future",
                label = "Taxe",
                amountCents = 12_000L,
                dayOfMonth = 15,
                status = be.monmoisclair.app.domain.model.ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        assertThat(archive("tpl-future")).isEqualTo(ArchiveExpenseTemplateResult.Success)
        assertThat(db.instanceDao().getExpenseById("exp:2027-03:tpl-future")).isNull()
        be.monmoisclair.app.domain.AppClock.clock =
            java.time.Clock.system(java.time.ZoneId.of("Europe/Brussels"))
    }

    @Test
    fun archive_removesLocalReceiptFilesOfDeletedFutureTodos() = runBlocking {
        be.monmoisclair.app.domain.AppClock.clock =
            java.time.Clock.fixed(
                java.time.Instant.parse("2026-11-05T10:00:00Z"),
                java.time.ZoneId.of("Europe/Brussels")
            )
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-photo",
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
        db.monthLedgerDao().insert(
            be.monmoisclair.app.data.db.entity.MonthLedgerEntity(
                "2027-03",
                "2027-03",
                createdAtEpochMs = 1L
            )
        )
        db.instanceDao().insertExpense(
            be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity(
                id = "exp:2027-03:tpl-photo",
                ledgerId = "2027-03",
                templateId = "tpl-photo",
                label = "Taxe",
                amountCents = 12_000L,
                dayOfMonth = 15,
                status = be.monmoisclair.app.domain.model.ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                receiptRelativePath = "receipts/future-photo.webp",
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        val receiptsDir = java.io.File(appContext.filesDir, "receipts").apply { mkdirs() }
        java.io.File(receiptsDir, "future-photo.webp").writeText("future")

        assertThat(archive("tpl-photo")).isEqualTo(ArchiveExpenseTemplateResult.Success)

        assertThat(java.io.File(receiptsDir, "future-photo.webp").exists()).isFalse()
        be.monmoisclair.app.domain.AppClock.clock =
            java.time.Clock.system(java.time.ZoneId.of("Europe/Brussels"))
    }
}
