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
}
