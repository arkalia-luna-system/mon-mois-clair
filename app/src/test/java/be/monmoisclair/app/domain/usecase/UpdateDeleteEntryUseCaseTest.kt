package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.reminder.PaymentNotificationHelper
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.data.widget.CarnetWidgetSink
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.MonthCycleEngine
import be.monmoisclair.app.domain.model.ExpenseStatus
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UpdateDeleteEntryUseCaseTest {

    private lateinit var appContext: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: MutatingRepository
    private lateinit var update: UpdateInstanceUseCase
    private lateinit var delete: DeleteInstanceUseCase
    private lateinit var add: AddEntryUseCase
    private val brussels = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        AppClock.clock = Clock.fixed(Instant.parse("2026-11-05T10:00:00Z"), brussels)
        appContext = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = MutatingRepository(
            db = db,
            backupScheduler = BackupScheduler {},
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) = Unit
            },
            widgetSink = CarnetWidgetSink.Noop
        )
        update = UpdateInstanceUseCase(appContext, db.instanceDao(), repo)
        delete = DeleteInstanceUseCase(
            appContext = appContext,
            instanceDao = db.instanceDao(),
            mutatingRepository = repo,
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) = Unit
            },
            paymentNotifications = PaymentNotificationHelper(appContext)
        )
        // AddEntry needs MonthCycleEngine - tested indirectly via seed
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
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

    @Test
    fun update_intervalMonths_updatesTemplateOnly() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-int",
                label = "Assur.",
                amountCents = 20_000L,
                dayOfMonth = 1,
                active = true,
                intervalMonths = 1,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        seedLinkedExpense("2026-10", "exp:2026-10:tpl-int", "tpl-int", 20_000L)

        val result = update(
            id = "exp:2026-10:tpl-int",
            isExpense = true,
            label = "Assur.",
            amountCents = 20_000L,
            dayOfMonth = 1,
            applyToFollowingMonths = false,
            intervalMonths = 12
        )
        assertThat(result).isEqualTo(UpdateInstanceResult.Success)
        assertThat(db.templateDao().getExpenseById("tpl-int")!!.intervalMonths).isEqualTo(12)
    }

    @Test
    fun update_once_to_monthly_creates_template() = runBlocking {
        if (db.monthLedgerDao().getById("2026-10") == null) {
            db.monthLedgerDao().insert(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L))
        }
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp-once",
                ledgerId = "2026-10",
                templateId = null,
                label = "Taxe",
                amountCents = 10_000L,
                dayOfMonth = 15,
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        val result = update(
            id = "exp-once",
            isExpense = true,
            label = "Taxe",
            amountCents = 10_000L,
            dayOfMonth = 15,
            applyToFollowingMonths = false,
            intervalMonths = 3
        )
        assertThat(result).isEqualTo(UpdateInstanceResult.Success)
        val updated = db.instanceDao().getExpenseById("exp-once")!!
        assertThat(updated.templateId).isNotNull()
        assertThat(db.templateDao().getExpenseById(updated.templateId!!)!!.intervalMonths)
            .isEqualTo(3)
    }

    @Test
    fun update_demote_to_once_archives_template_and_clears_link() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-demote",
                label = "Gaz",
                amountCents = 8_000L,
                dayOfMonth = 2,
                active = true,
                intervalMonths = 1,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        seedLinkedExpense("2026-10", "exp:2026-10:tpl-demote", "tpl-demote", 8_000L)
        seedLinkedExpense("2026-11", "exp:2026-11:tpl-demote", "tpl-demote", 8_000L)

        val result = update(
            id = "exp:2026-10:tpl-demote",
            isExpense = true,
            label = "Gaz",
            amountCents = 8_000L,
            dayOfMonth = 2,
            applyToFollowingMonths = false,
            intervalMonths = -1
        )
        assertThat(result).isEqualTo(UpdateInstanceResult.Success)
        assertThat(db.instanceDao().getExpenseById("exp:2026-10:tpl-demote")!!.templateId).isNull()
        assertThat(db.templateDao().getExpenseById("tpl-demote")!!.active).isFalse()
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-demote")).isNull()
    }

    @Test
    fun update_expense_togglesAutoDebit_andPropagatesToTemplate() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-auto",
                label = "Loyer",
                amountCents = 50_000L,
                dayOfMonth = 1,
                active = true,
                isAutoDebit = false,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        seedLinkedExpense("2026-10", "exp:2026-10:tpl-auto", "tpl-auto", 50_000L)
        seedLinkedExpense("2026-11", "exp:2026-11:tpl-auto", "tpl-auto", 50_000L)

        val result = update(
            id = "exp:2026-10:tpl-auto",
            isExpense = true,
            label = "Loyer",
            amountCents = 50_000L,
            dayOfMonth = 1,
            applyToFollowingMonths = true,
            isAutoDebit = true
        )
        assertThat(result).isEqualTo(UpdateInstanceResult.Success)
        assertThat(db.instanceDao().getExpenseById("exp:2026-10:tpl-auto")!!.isAutoDebit).isTrue()
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-auto")!!.isAutoDebit).isTrue()
        assertThat(db.templateDao().getExpenseById("tpl-auto")!!.isAutoDebit).isTrue()
    }

    /**
     * B01 : facture ordinaire — note → modèle sans « mois suivants »,
     * mais **pas** les autres mois déjà créés (ref souvent variable).
     * Photo seulement avec « Aussi les mois suivants ».
     */
    @Test
    fun update_paymentNote_ordinary_templateOnly_untilApplyFollowing() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-note",
                label = "Gaz",
                amountCents = 8_000L,
                dayOfMonth = 3,
                active = true,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        seedLinkedExpense("2026-10", "exp:2026-10:tpl-note", "tpl-note", 8_000L)
        seedLinkedExpense("2026-11", "exp:2026-11:tpl-note", "tpl-note", 8_000L)

        update(
            id = "exp:2026-10:tpl-note",
            isExpense = true,
            label = "Gaz",
            amountCents = 8_000L,
            dayOfMonth = 3,
            applyToFollowingMonths = false,
            paymentNote = "+++000/0000/00097+++",
            receiptRelativePath = "receipts/oct.webp"
        )
        val tplAfterNote = db.templateDao().getExpenseById("tpl-note")!!
        assertThat(tplAfterNote.paymentNote).isEqualTo("+++000/0000/00097+++")
        assertThat(tplAfterNote.receiptRelativePath).isEqualTo("")
        assertThat(db.instanceDao().getExpenseById("exp:2026-10:tpl-note")!!.paymentNote)
            .isEqualTo("+++000/0000/00097+++")
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-note")!!.paymentNote)
            .isEmpty()
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-note")!!.receiptRelativePath)
            .isEqualTo("")

        update(
            id = "exp:2026-10:tpl-note",
            isExpense = true,
            label = "Gaz",
            amountCents = 8_000L,
            dayOfMonth = 3,
            applyToFollowingMonths = true,
            paymentNote = "+++000/0000/00097+++",
            receiptRelativePath = "receipts/canon.webp"
        )
        val tpl = db.templateDao().getExpenseById("tpl-note")!!
        assertThat(tpl.receiptRelativePath).isEqualTo("receipts/canon.webp")
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-note")!!.paymentNote)
            .isEqualTo("+++000/0000/00097+++")
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-note")!!.receiptRelativePath)
            .isEqualTo("receipts/canon.webp")
    }

    @Test
    fun update_clearReceipt_removesThisMonthOnly() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-photo",
                label = "Eau",
                amountCents = 4_000L,
                dayOfMonth = 8,
                active = true,
                receiptRelativePath = "receipts/modele.webp",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        seedLinkedExpense("2026-10", "exp:2026-10:tpl-photo", "tpl-photo", 4_000L)
        db.instanceDao().updateExpense(
            db.instanceDao().getExpenseById("exp:2026-10:tpl-photo")!!.copy(
                receiptRelativePath = "receipts/octobre.webp"
            )
        )
        update(
            id = "exp:2026-10:tpl-photo",
            isExpense = true,
            label = "Eau",
            amountCents = 4_000L,
            dayOfMonth = 8,
            applyToFollowingMonths = false,
            clearReceipt = true
        )
        assertThat(db.instanceDao().getExpenseById("exp:2026-10:tpl-photo")!!.receiptRelativePath)
            .isEqualTo("")
        assertThat(db.templateDao().getExpenseById("tpl-photo")!!.receiptRelativePath)
            .isEqualTo("receipts/modele.webp")
    }

    @Test
    fun update_clearReceipt_oneOffDeletesLocalReceiptFile() = runBlocking {
        if (db.monthLedgerDao().getById("2026-10") == null) {
            db.monthLedgerDao().insert(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L))
        }
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp-oneoff-photo",
                ledgerId = "2026-10",
                templateId = null,
                label = "Taxe",
                amountCents = 10_000L,
                dayOfMonth = 15,
                status = ExpenseStatus.TODO,
                doneAtEpochMs = null,
                remindJ3 = true,
                remindJd = true,
                receiptRelativePath = "receipts/oneoff.webp",
                alarmJ3RequestCode = null,
                alarmJdRequestCode = null,
                createdAtEpochMs = 1L
            )
        )
        val receiptsDir = java.io.File(appContext.filesDir, "receipts").apply { mkdirs() }
        java.io.File(receiptsDir, "oneoff.webp").writeText("oneoff")

        val result = update(
            id = "exp-oneoff-photo",
            isExpense = true,
            label = "Taxe",
            amountCents = 10_000L,
            dayOfMonth = 15,
            applyToFollowingMonths = false,
            clearReceipt = true
        )

        assertThat(result).isEqualTo(UpdateInstanceResult.Success)
        assertThat(db.instanceDao().getExpenseById("exp-oneoff-photo")!!.receiptRelativePath).isEmpty()
        assertThat(java.io.File(receiptsDir, "oneoff.webp").exists()).isFalse()
    }

    /** Lot C : remplacer / ajouter des courriers en plus sur le modèle. */
    @Test
    fun update_extraReceiptPaths_replaceThenAppend() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-extra",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 10,
                active = true,
                openingRemainingCents = 20_000L,
                extraReceiptPaths = "receipts/a.webp\nreceipts/b.webp",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        seedLinkedExpense("2026-10", "exp:2026-10:tpl-extra", "tpl-extra", 5_000L)

        update(
            id = "exp:2026-10:tpl-extra",
            isExpense = true,
            label = "Étude",
            amountCents = 5_000L,
            dayOfMonth = 10,
            applyToFollowingMonths = false,
            extraReceiptPathsReplace = "receipts/a.webp",
            extraReceiptAppend = "receipts/c.webp"
        )
        val tpl = db.templateDao().getExpenseById("tpl-extra")!!
        assertThat(tpl.extraReceiptPaths).isEqualTo("receipts/a.webp\nreceipts/c.webp")
    }

    /** Taxe annuelle : montant changeable sur un mois futur visualisé (7.20). */
    @Test
    fun update_futureMonthInstance_isAllowed() = runBlocking {
        seedLinkedExpense("2027-03", "exp:2027-03:taxe", "tpl-taxe", 250_00L)
        val result = update(
            id = "exp:2027-03:taxe",
            isExpense = true,
            label = "Taxe voiture",
            amountCents = 280_00L,
            dayOfMonth = 15,
            applyToFollowingMonths = false
        )
        assertThat(result).isEqualTo(UpdateInstanceResult.Success)
        assertThat(db.instanceDao().getExpenseById("exp:2027-03:taxe")!!.amountCents)
            .isEqualTo(280_00L)
        assertThat(db.instanceDao().getExpenseById("exp:2027-03:taxe")!!.label)
            .isEqualTo("Taxe voiture")
    }

    @Test
    fun delete_futureMonthInstance_isAllowed() = runBlocking {
        seedLinkedExpense("2027-04", "exp:2027-04:once", "tpl-once", 40_00L)
        val result = delete(
            id = "exp:2027-04:once",
            isExpense = true,
            applyToFollowingMonths = false
        )
        assertThat(result).isEqualTo(DeleteInstanceResult.Success)
        assertThat(db.instanceDao().getExpenseById("exp:2027-04:once")).isNull()
    }

    /** 7.32 : delete « aussi mois suivants » annule les alarmes de toutes les lignes retirées. */
    @Test
    fun delete_following_cancelsRemindersForAllRemovedTodos() = runBlocking {
        val cancelled = mutableListOf<String>()
        val localDelete = DeleteInstanceUseCase(
            appContext = appContext,
            instanceDao = db.instanceDao(),
            mutatingRepository = repo,
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) {
                    cancelled += expenseId
                }
            },
            paymentNotifications = PaymentNotificationHelper(appContext)
        )
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-loyer",
                label = "Loyer",
                amountCents = 800_00L,
                dayOfMonth = 1,
                active = true,
                intervalMonths = 1,
                anchorYearMonth = "2026-11",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        seedLinkedExpense(
            ledger = "2026-11",
            id = "exp:2026-11:tpl-loyer",
            templateId = "tpl-loyer",
            amount = 800_00L
        )
        seedLinkedExpense(
            ledger = "2026-12",
            id = "exp:2026-12:tpl-loyer",
            templateId = "tpl-loyer",
            amount = 800_00L
        )
        seedLinkedExpense(
            ledger = "2027-01",
            id = "exp:2027-01:tpl-loyer",
            templateId = "tpl-loyer",
            amount = 800_00L
        )
        assertThat(
            localDelete(
                id = "exp:2026-11:tpl-loyer",
                isExpense = true,
                applyToFollowingMonths = true
            )
        ).isEqualTo(DeleteInstanceResult.Success)
        assertThat(cancelled).containsExactly(
            "exp:2026-11:tpl-loyer",
            "exp:2026-12:tpl-loyer",
            "exp:2027-01:tpl-loyer"
        )
        assertThat(db.instanceDao().getExpenseById("exp:2026-12:tpl-loyer")).isNull()
        assertThat(db.templateDao().getExpenseById("tpl-loyer")!!.active).isFalse()
    }

    @Test
    fun delete_following_removesLocalReceiptFilesOfDeletedTodos() = runBlocking {
        val localDelete = DeleteInstanceUseCase(
            appContext = appContext,
            instanceDao = db.instanceDao(),
            mutatingRepository = repo,
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) = Unit
            },
            paymentNotifications = PaymentNotificationHelper(appContext)
        )
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-photo",
                label = "Loyer",
                amountCents = 800_00L,
                dayOfMonth = 1,
                active = true,
                intervalMonths = 1,
                anchorYearMonth = "2026-11",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        seedLinkedExpense("2026-11", "exp:2026-11:tpl-photo", "tpl-photo", 800_00L)
        seedLinkedExpense("2026-12", "exp:2026-12:tpl-photo", "tpl-photo", 800_00L)
        db.instanceDao().updateExpense(
            db.instanceDao().getExpenseById("exp:2026-11:tpl-photo")!!.copy(
                receiptRelativePath = "receipts/current.webp"
            )
        )
        db.instanceDao().updateExpense(
            db.instanceDao().getExpenseById("exp:2026-12:tpl-photo")!!.copy(
                receiptRelativePath = "receipts/future.pdf"
            )
        )
        val receiptsDir = java.io.File(appContext.filesDir, "receipts").apply { mkdirs() }
        java.io.File(receiptsDir, "current.webp").writeText("current")
        java.io.File(receiptsDir, "future.pdf").writeText("future")

        assertThat(
            localDelete(
                id = "exp:2026-11:tpl-photo",
                isExpense = true,
                applyToFollowingMonths = true
            )
        ).isEqualTo(DeleteInstanceResult.Success)

        assertThat(java.io.File(receiptsDir, "current.webp").exists()).isFalse()
        assertThat(java.io.File(receiptsDir, "future.pdf").exists()).isFalse()
    }

    /** B1 7.28 : delete « ce mois seulement » + ensureMonth ne doit pas ressusciter. */
    @Test
    fun delete_thisMonthOnly_thenEnsureMonth_doesNotResurrect() = runBlocking {
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-loyer",
                label = "Loyer",
                amountCents = 800_00L,
                dayOfMonth = 1,
                active = true,
                intervalMonths = 1,
                anchorYearMonth = "2026-11",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        seedLinkedExpense(
            ledger = "2026-11",
            id = "exp:2026-11:tpl-loyer",
            templateId = "tpl-loyer",
            amount = 800_00L
        )
        val deleted = delete(
            id = "exp:2026-11:tpl-loyer",
            isExpense = true,
            applyToFollowingMonths = false
        )
        assertThat(deleted).isEqualTo(DeleteInstanceResult.Success)
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-loyer")).isNull()

        val engine = MonthCycleEngine(db.monthLedgerDao(), repo)
        engine.ensureMonthExists(YearMonth.parse("2026-11"))
        assertThat(db.instanceDao().getExpenseById("exp:2026-11:tpl-loyer")).isNull()
    }

    private suspend fun seedLinkedExpense(
        ledger: String,
        id: String,
        templateId: String,
        amount: Long
    ) {
        if (db.monthLedgerDao().getById(ledger) == null) {
            db.monthLedgerDao().insert(MonthLedgerEntity(ledger, ledger, createdAtEpochMs = 1L))
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
