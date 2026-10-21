package be.monmoisclair.app.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE expense_templates ADD COLUMN isAutoDebit INTEGER NOT NULL DEFAULT 0"
            )
            db.execSQL(
                "ALTER TABLE expense_templates ADD COLUMN intervalMonths INTEGER NOT NULL DEFAULT 1"
            )
            db.execSQL(
                "ALTER TABLE expense_templates ADD COLUMN anchorYearMonth TEXT NOT NULL DEFAULT ''"
            )
            db.execSQL(
                "ALTER TABLE expense_instances ADD COLUMN isAutoDebit INTEGER NOT NULL DEFAULT 0"
            )
            db.execSQL(
                "ALTER TABLE month_ledgers ADD COLUMN diversSpentCents INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE expense_instances ADD COLUMN paymentNote TEXT NOT NULL DEFAULT ''"
            )
            db.execSQL(
                "ALTER TABLE expense_instances ADD COLUMN receiptRelativePath TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    /** Lot A : mémoire papier sur le modèle (communication + photo). */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE expense_templates ADD COLUMN paymentNote TEXT NOT NULL DEFAULT ''"
            )
            db.execSQL(
                "ALTER TABLE expense_templates ADD COLUMN receiptRelativePath TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    /** Lot B : dossier / accord (cause + reste de départ). */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE expense_templates ADD COLUMN causeLabel TEXT NOT NULL DEFAULT ''"
            )
            db.execSQL(
                "ALTER TABLE expense_templates ADD COLUMN openingRemainingCents INTEGER"
            )
        }
    }

    /** Lot C : IBAN, e-mail, photos en plus, frais. */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE expense_templates ADD COLUMN iban TEXT NOT NULL DEFAULT ''"
            )
            db.execSQL(
                "ALTER TABLE expense_templates ADD COLUMN creditorEmail TEXT NOT NULL DEFAULT ''"
            )
            db.execSQL(
                "ALTER TABLE expense_templates ADD COLUMN extraReceiptPaths TEXT NOT NULL DEFAULT ''"
            )
            db.execSQL(
                "ALTER TABLE expense_templates ADD COLUMN addedFeesCents INTEGER NOT NULL DEFAULT 0"
            )
            db.execSQL(
                "ALTER TABLE expense_templates ADD COLUMN addedFeesNote TEXT NOT NULL DEFAULT ''"
            )
        }
    }
}
