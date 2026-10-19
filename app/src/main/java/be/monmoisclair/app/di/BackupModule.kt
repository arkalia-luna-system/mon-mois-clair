package be.monmoisclair.app.di

import be.monmoisclair.app.data.backup.BackupJsonCodec
import be.monmoisclair.app.data.backup.BackupRotator
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.backup.WorkManagerBackupScheduler
import be.monmoisclair.app.data.reminder.NoOpReminderScheduler
import be.monmoisclair.app.data.reminder.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupBindingsModule {

    @Binds
    @Singleton
    abstract fun bindBackupScheduler(impl: WorkManagerBackupScheduler): BackupScheduler

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(impl: NoOpReminderScheduler): ReminderScheduler
}

@Module
@InstallIn(SingletonComponent::class)
object BackupProvidesModule {

    @Provides
    @Singleton
    fun provideBackupJsonCodec(): BackupJsonCodec = BackupJsonCodec()

    @Provides
    @Singleton
    fun provideBackupRotator(): BackupRotator = BackupRotator()
}
