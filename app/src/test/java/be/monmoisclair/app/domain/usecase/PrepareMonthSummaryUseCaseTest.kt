package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.data.db.entity.ExpenseInstanceEntity
import be.monmoisclair.app.data.db.entity.ExpenseTemplateEntity
import be.monmoisclair.app.data.db.entity.IncomeInstanceEntity
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.AppClock
import be.monmoisclair.app.domain.model.AppMetaKeys
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
import java.time.YearMonth
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class PrepareMonthSummaryUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var prepare: PrepareMonthSummaryUseCase
    private val brussels: ZoneId = ZoneId.of("Europe/Brussels")

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val list = ListDossiersUseCase(db.templateDao(), db.instanceDao())
        prepare = PrepareMonthSummaryUseCase(
            instanceDao = db.instanceDao(),
            templateDao = db.templateDao(),
            appMetaDao = db.appMetaDao(),
            listDossiers = list
        )
        AppClock.clock = Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), brussels)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.system(brussels)
        db.close()
    }

    @Test
    fun text_hasFourDebtColumns_andNoFilePath_noDebtTotalTitle() = runBlocking {
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L))
        db.instanceDao().insertIncome(
            IncomeInstanceEntity(
                id = "inc",
                ledgerId = "2026-10",
                templateId = null,
                label = "Salaire",
                amountCents = 100_000L,
                dayOfMonth = 1,
                createdAtEpochMs = 1L
            )
        )
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-acc",
                label = "Étude",
                amountCents = 5_000L,
                dayOfMonth = 12,
                active = true,
                causeLabel = "Hôpital",
                openingRemainingCents = 60_000L,
                paymentNote = "+++000/0000/00097+++",
                receiptRelativePath = "receipts/secret.webp",
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp-acc",
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

        val result = prepare(YearMonth.of(2026, 10))
        val text = result.text
        assertThat(text).contains("À qui : Étude")
        assertThat(text).contains("Pour quoi : Hôpital")
        assertThat(text).contains("Versement ce mois :")
        assertThat(text).contains("Reste :")
        assertThat(text).contains("Encore environ")
        assertThat(text).contains("si le versement reste de")
        assertThat(text).doesNotContain("fin prévue")
        assertThat(text).contains("Référence : +++000/0000/00097+++")
        assertThat(text).doesNotContain("receipts/")
        assertThat(text).doesNotContain("secret.webp")
        assertThat(text.lowercase()).doesNotContain("total des dettes")
        assertThat(text).contains("Les photos des courriers sont dans l’application.")
        assertThat(result.isDeficit).isFalse()
    }

    @Test
    fun deficit_mentionsWordAndAmount_withoutPayOrderAdvice() = runBlocking {
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L))
        db.instanceDao().insertIncome(
            IncomeInstanceEntity(
                id = "inc",
                ledgerId = "2026-10",
                templateId = null,
                label = "Salaire",
                amountCents = 50_000L,
                dayOfMonth = 1,
                createdAtEpochMs = 1L
            )
        )
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp",
                ledgerId = "2026-10",
                templateId = null,
                label = "Loyer",
                amountCents = 80_000L,
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
        val text = prepare(YearMonth.of(2026, 10)).text
        assertThat(text).contains("Déficit")
        assertThat(text).contains("300")
        assertThat(text.lowercase()).doesNotContain("payez d’abord")
        assertThat(text.lowercase()).doesNotContain("payez d'abord")
    }

    @Test
    fun offMonthDossier_saysNoPaymentThisMonth_andKeepsPace() = runBlocking {
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L))
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
        val text = prepare(YearMonth.of(2026, 10)).text
        assertThat(text).contains("À qui : Taxe")
        assertThat(text).contains("Pas de versement ce mois.")
        assertThat(text).doesNotContain("Versement ce mois :")
        assertThat(text).contains("Encore environ 1 versement si le versement reste de 120 €.")
        assertThat(text).contains("Vers octobre 2026.")
        assertThat(text).doesNotContain("fin prévue")
    }

    @Test
    fun savings_listedApart_notUnderDossiers() = runBlocking {
        db.monthLedgerDao().insert(MonthLedgerEntity("2026-10", "2026-10", createdAtEpochMs = 1L))
        db.templateDao().insertExpense(
            ExpenseTemplateEntity(
                id = "tpl-sav",
                label = ConfigureSavingsUseCase.LABEL,
                amountCents = 5_000L,
                dayOfMonth = 10,
                active = true,
                createdAtEpochMs = 1L,
                archivedAtEpochMs = null
            )
        )
        db.appMetaDao().upsert(AppMetaEntity(AppMetaKeys.SAVINGS_TEMPLATE_ID, "tpl-sav"))
        db.instanceDao().insertExpense(
            ExpenseInstanceEntity(
                id = "exp-sav",
                ledgerId = "2026-10",
                templateId = "tpl-sav",
                label = ConfigureSavingsUseCase.LABEL,
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
        val text = prepare(YearMonth.of(2026, 10)).text
        assertThat(text).contains("Mis de côté")
        assertThat(text).contains(ConfigureSavingsUseCase.LABEL)
        // Pas sous « Dossiers » comme dette.
        val dossiersBlock = text.substringAfter("Dossiers (accords)").substringBefore("Mis de côté")
        assertThat(dossiersBlock).doesNotContain(ConfigureSavingsUseCase.LABEL)
    }
}
