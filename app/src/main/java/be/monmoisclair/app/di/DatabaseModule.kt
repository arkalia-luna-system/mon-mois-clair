package be.monmoisclair.app.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import be.monmoisclair.app.data.crypto.DbPassphraseVault
import be.monmoisclair.app.data.crypto.EncryptedDbMigrator
import be.monmoisclair.app.data.crypto.LocalFileCipher
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.Migrations
import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.dao.ExpenseFeeEventDao
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.MonthLedgerDao
import be.monmoisclair.app.data.db.dao.PendingLetterDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.BackupHealth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val TAG = "DatabaseModule"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        vault: DbPassphraseVault
    ): AppDatabase {
        // Robolectric / JVM host : pas de libsqlcipher.so → Room clair (tests).
        // Device / émulateur ARM : SQLCipher obligatoire après migration.
        val sqlCipherLoaded = runCatching {
            System.loadLibrary("sqlcipher")
            true
        }.getOrDefault(false)

        if (!sqlCipherLoaded) {
            Log.w(TAG, "SQLCipher indisponible (tests JVM) — Room non chiffré")
            return buildRoom(context, vault, factory = null)
        }

        vault.ensureProvisioned()
        val outcome = EncryptedDbMigrator.migrateIfNeeded(context, vault)
        when (outcome) {
            EncryptedDbMigrator.MigrationOutcome.Failed -> {
                Log.e(TAG, "Migration SQLCipher échouée — ouverture claire temporaire")
                // P1 : marqueur CE pour bannière Réglages (message utilisatrice claire).
                runCatching { vault.markMigrationFailed() }
            }
            EncryptedDbMigrator.MigrationOutcome.Migrated -> {
                Log.i(TAG, "DB migrée vers SQLCipher")
                runCatching { vault.clearMigrationFailed() }
            }
            EncryptedDbMigrator.MigrationOutcome.AlreadyEncrypted,
            EncryptedDbMigrator.MigrationOutcome.NoPlaintextDb ->
                runCatching { vault.clearMigrationFailed() }
        }

        runCatching {
            LocalFileCipher.migrateDirectory(File(context.filesDir, "receipts"))
        }.onFailure { Log.w(TAG, "migration receipts", it) }

        val openEncrypted = when (outcome) {
            EncryptedDbMigrator.MigrationOutcome.Failed ->
                vault.isDbEncryptedMarkerPresent()
            else -> true
        }

        val factory = if (openEncrypted) {
            val passphrase = vault.unwrapPassphraseChars()
            try {
                SupportOpenHelperFactory(String(passphrase).toByteArray(Charsets.UTF_8))
            } finally {
                passphrase.fill('\u0000')
            }
        } else {
            null
        }

        return buildRoom(context, vault, factory)
    }

    private fun buildRoom(
        context: Context,
        vault: DbPassphraseVault,
        factory: SupportOpenHelperFactory?
    ): AppDatabase {
        val builder = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DbPassphraseVault.DB_NAME
        )
            .addMigrations(
                Migrations.MIGRATION_1_2,
                Migrations.MIGRATION_2_3,
                Migrations.MIGRATION_3_4,
                Migrations.MIGRATION_4_5,
                Migrations.MIGRATION_5_6,
                Migrations.MIGRATION_6_7,
                Migrations.MIGRATION_7_8
            )
            .addCallback(roomCallback(vault, markEncrypted = factory != null))
        if (factory != null) {
            builder.openHelperFactory(factory)
        }
        return builder.build()
    }

    private fun roomCallback(
        vault: DbPassphraseVault,
        markEncrypted: Boolean
    ) = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // OR IGNORE : après sqlcipher_export, Room peut rappeler onCreate si
            // user_version=0 (UNIQUE app_meta.key sinon → crash à l’ouverture).
            db.execSQL(
                "INSERT OR IGNORE INTO app_meta (`key`, value) VALUES (?, ?)",
                arrayOf(AppMetaKeys.SCHEMA_VERSION, AppMetaKeys.CURRENT_SCHEMA_VERSION)
            )
            db.execSQL(
                "INSERT OR IGNORE INTO app_meta (`key`, value) VALUES (?, ?)",
                arrayOf(AppMetaKeys.BACKUP_HEALTH, BackupHealth.NOT_CONFIGURED.name)
            )
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            if (!markEncrypted) return
            runCatching {
                val cursor = db.query("PRAGMA cipher_version")
                val hasCipher = cursor.use { it.moveToFirst() && !it.isNull(0) }
                if (hasCipher) vault.markDbEncrypted()
            }
        }
    }

    @Provides
    fun provideMonthLedgerDao(db: AppDatabase): MonthLedgerDao = db.monthLedgerDao()

    @Provides
    fun provideTemplateDao(db: AppDatabase): TemplateDao = db.templateDao()

    @Provides
    fun provideInstanceDao(db: AppDatabase): InstanceDao = db.instanceDao()

    @Provides
    fun provideAppMetaDao(db: AppDatabase): AppMetaDao = db.appMetaDao()

    @Provides
    fun providePendingLetterDao(db: AppDatabase): PendingLetterDao = db.pendingLetterDao()

    @Provides
    fun provideExpenseFeeEventDao(db: AppDatabase): ExpenseFeeEventDao = db.expenseFeeEventDao()
}
