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

    /** Lot D : courriers sans plan (pas de montant, pas dans le Reste). */
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `pending_letters` (" +
                    "`id` TEXT NOT NULL, " +
                    "`who` TEXT NOT NULL, " +
                    "`what` TEXT NOT NULL, " +
                    "`receiptRelativePath` TEXT NOT NULL, " +
                    "`paymentNote` TEXT NOT NULL, " +
                    "`iban` TEXT NOT NULL, " +
                    "`creditorEmail` TEXT NOT NULL, " +
                    "`createdAtEpochMs` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`id`))"
            )
        }
    }

    /** Liste datée des frais (lettres successives) ; backfill depuis le cumul lot C. */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `expense_fee_events` (" +
                    "`id` TEXT NOT NULL, " +
                    "`templateId` TEXT NOT NULL, " +
                    "`amountCents` INTEGER NOT NULL, " +
                    "`note` TEXT NOT NULL, " +
                    "`createdAtEpochMs` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`id`), " +
                    "FOREIGN KEY(`templateId`) REFERENCES `expense_templates`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_expense_fee_events_templateId` " +
                    "ON `expense_fee_events` (`templateId`)"
            )
            db.execSQL(
                "INSERT INTO `expense_fee_events` " +
                    "(`id`, `templateId`, `amountCents`, `note`, `createdAtEpochMs`) " +
                    "SELECT 'fee-migrated-' || `id`, `id`, `addedFeesCents`, `addedFeesNote`, " +
                    "`createdAtEpochMs` FROM `expense_templates` WHERE `addedFeesCents` > 0"
            )
        }
    }
}
