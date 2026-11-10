package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.data.widget.CarnetWidgetSink
import be.monmoisclair.app.domain.AppClock
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UpdateInstanceReceiptExtrasCleanupTest {

    private lateinit var appContext: Context
    private lateinit var db: AppDatabase
    private lateinit var update: UpdateInstanceUseCase

    @Before
    fun setUp() {
        AppClock.clock = Clock.fixed(Instant.parse("2026-11-05T10:00:00Z"), ZoneId.of("Europe/Brussels"))
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
        update = UpdateInstanceUseCase(appContext, db.instanceDao(), repo)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(ZoneId.of("Europe/Brussels"))
        db.close()
    }

    @Test
    fun update_replaceExtraReceipts_deletesOnlyRemovedLocalFiles() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-extra-clean",
                label = "Etude",
                amountCents = 5_000L,
                dayOfMonth = 10,
                active = true,
                extraReceiptPaths = "receipts/a.webp\nreceipts/b.pdf",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        if (db.monthLedgerDao().getById("2026-10") == null) {
            db.monthLedgerDao().insert(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L))
        }
        db.instanceDao().insertExpense(
            be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity(
                id = "exp:2026-10:tpl-extra-clean",
                ledgerId = "2026-10",
                templateId = "tpl-extra-clean",
                label = "Etude",
                amountCents = 5_000L,
                dayOfMonth = 10,
                status = be.monmoisclair.app.domain.model.ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        val receiptsDir = java.io.File(appContext.filesDir, "receipts").apply { mkdirs() }
        java.io.File(receiptsDir, "a.webp").writeText("keep")
        java.io.File(receiptsDir, "b.pdf").writeText("drop")

        val result = update(
            id = "exp:2026-10:tpl-extra-clean",
            isExpense = true,
            label = "Etude",
            amountCents = 5_000L,
            dayOfMonth = 10,
            applyToFollowingMonths = false,
            extraReceiptPathsReplace = "receipts/a.webp"
        )

        assertThat(result).isEqualTo(UpdateInstanceResult.Success)
        assertThat(db.templateDao().getExpenseById("tpl-extra-clean")!!.extraReceiptPaths)
            .isEqualTo("receipts/a.webp")
        assertThat(java.io.File(receiptsDir, "a.webp").exists()).isTrue()
        assertThat(java.io.File(receiptsDir, "b.pdf").exists()).isFalse()
    }
}
