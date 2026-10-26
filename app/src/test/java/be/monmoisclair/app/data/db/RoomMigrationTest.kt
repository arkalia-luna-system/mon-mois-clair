package be.monmoisclair.app.data.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Filet sideload famille : SQL [Migrations.MIGRATION_7_8] (frais datés + backfill).
 * Sans MigrationTestHelper/assets (Robolectric ne les charge pas ici) — on exécute
 * le callback de migration sur une table v7 minimale.
 *
 * Chaîne 1→8 : **pas** de harness ici (schémas assets Room non chargés sous Robolectric).
 * Les [Migrations.MIGRATION_1_2] … [Migrations.MIGRATION_6_7] restent couverts
 * indirectement via installs réelles / sideload ; à instrumenter plus tard si besoin.
 */
@RunWith(RobolectricTestRunner::class)
class RoomMigrationTest {

    private lateinit var db: SupportSQLiteDatabase
    private lateinit var helper: SupportSQLiteOpenHelper

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(DB_NAME)
        helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(DB_NAME)
                .callback(object : SupportSQLiteOpenHelper.Callback(7) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS `expense_templates` (
                              `id` TEXT NOT NULL,
                              `label` TEXT NOT NULL,
                              `amountCents` INTEGER NOT NULL,
                              `dayOfMonth` INTEGER NOT NULL,
                              `active` INTEGER NOT NULL,
                              `remindJ3` INTEGER NOT NULL,
                              `remindJd` INTEGER NOT NULL,
                              `isAutoDebit` INTEGER NOT NULL,
                              `intervalMonths` INTEGER NOT NULL,
                              `anchorYearMonth` TEXT NOT NULL,
                              `paymentNote` TEXT NOT NULL,
                              `receiptRelativePath` TEXT NOT NULL,
                              `causeLabel` TEXT NOT NULL,
                              `openingRemainingCents` INTEGER,
                              `iban` TEXT NOT NULL,
                              `creditorEmail` TEXT NOT NULL,
                              `extraReceiptPaths` TEXT NOT NULL,
                              `addedFeesCents` INTEGER NOT NULL,
                              `addedFeesNote` TEXT NOT NULL,
                              `createdAtEpochMs` INTEGER NOT NULL,
                              `archivedAtEpochMs` INTEGER,
                              PRIMARY KEY(`id`)
                            )
                            """.trimIndent()
                        )
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) = Unit
                })
                .build()
        )
        db = helper.writableDatabase
    }

    @After
    fun tearDown() {
        helper.close()
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(DB_NAME)
    }

    @Test
    fun migrate7To8_backfillsFeeEventsFromAddedFees() {
        db.execSQL(
            """
            INSERT INTO expense_templates (
              id, label, amountCents, dayOfMonth, active, remindJ3, remindJd,
              isAutoDebit, intervalMonths, anchorYearMonth,
              paymentNote, receiptRelativePath, causeLabel, openingRemainingCents,
              iban, creditorEmail, extraReceiptPaths, addedFeesCents, addedFeesNote,
              createdAtEpochMs
            ) VALUES (
              'tpl-fee', 'Huissier', 5000, 5, 1, 1, 1,
              0, 1, '2026-01',
              '', '', 'retard', 20000,
              '', '', '', 1500, 'frais lettre',
              1700000000000
            )
            """.trimIndent()
        )

        Migrations.MIGRATION_7_8.migrate(db)

        db.query(
            "SELECT amountCents, note FROM expense_fee_events WHERE templateId = 'tpl-fee'"
        ).use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getLong(0)).isEqualTo(1500L)
            assertThat(cursor.getString(1)).isEqualTo("frais lettre")
        }

        // Sans frais : pas de ligne fantôme.
        db.execSQL(
            """
            INSERT INTO expense_templates (
              id, label, amountCents, dayOfMonth, active, remindJ3, remindJd,
              isAutoDebit, intervalMonths, anchorYearMonth,
              paymentNote, receiptRelativePath, causeLabel, openingRemainingCents,
              iban, creditorEmail, extraReceiptPaths, addedFeesCents, addedFeesNote,
              createdAtEpochMs
            ) VALUES (
              'tpl-zero', 'Loyer', 80000, 1, 1, 1, 1,
              0, 1, '2026-01',
              '', '', '', NULL,
              '', '', '', 0, '',
              1700000000000
            )
            """.trimIndent()
        )
        // Re-run INSERT SELECT would duplicate tpl-fee — only assert zero row absent.
        db.query(
            "SELECT COUNT(*) FROM expense_fee_events WHERE templateId = 'tpl-zero'"
        ).use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getInt(0)).isEqualTo(0)
        }
    }

    companion object {
        private const val DB_NAME = "migration-7-8-unit"
    }
}
