package be.monmoisclair.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.Migrations
import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.dao.InstanceDao
import be.monmoisclair.app.data.db.dao.MonthLedgerDao
import be.monmoisclair.app.data.db.dao.TemplateDao
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.BackupHealth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mon_mois_clair.db"
        )
            .addMigrations(Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL(
                        "INSERT INTO app_meta (`key`, value) VALUES (?, ?)",
                        arrayOf(AppMetaKeys.SCHEMA_VERSION, AppMetaKeys.CURRENT_SCHEMA_VERSION)
                    )
                    db.execSQL(
                        "INSERT INTO app_meta (`key`, value) VALUES (?, ?)",
                        arrayOf(AppMetaKeys.BACKUP_HEALTH, BackupHealth.NOT_CONFIGURED.name)
                    )
                }
            })
            .build()
    }

    @Provides
    fun provideMonthLedgerDao(db: AppDatabase): MonthLedgerDao = db.monthLedgerDao()

    @Provides
    fun provideTemplateDao(db: AppDatabase): TemplateDao = db.templateDao()

    @Provides
    fun provideInstanceDao(db: AppDatabase): InstanceDao = db.instanceDao()

    @Provides
    fun provideAppMetaDao(db: AppDatabase): AppMetaDao = db.appMetaDao()
}
