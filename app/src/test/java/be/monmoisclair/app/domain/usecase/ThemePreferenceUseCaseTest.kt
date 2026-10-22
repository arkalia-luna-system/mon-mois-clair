package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.ThemePreferenceCache
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.ThemePreference
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicInteger
import be.monmoisclair.app.data.widget.CarnetWidgetSink

@RunWith(RobolectricTestRunner::class)
class ThemePreferenceUseCaseTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var useCase: ThemePreferenceUseCase
    private val backupCalls = AtomicInteger(0)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("theme_preference_cache", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backupCalls.set(0)
        val mutating = MutatingRepository(
            db = db,
            backupScheduler = object : BackupScheduler {
                override fun enqueueImmediate() {
                    backupCalls.incrementAndGet()
                }
            },
            reminderScheduler = object : ReminderScheduler {
                override suspend fun reconcileAll() = Unit
                override suspend fun cancelFor(expenseId: String) = Unit
            },
            widgetSink = CarnetWidgetSink.Noop
        )
        useCase = ThemePreferenceUseCase(context, db.appMetaDao(), mutating)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun default_is_system() = runBlocking {
        assertThat(useCase.read()).isEqualTo(ThemePreference.System)
        assertThat(useCase.preference.value).isEqualTo(ThemePreference.System)
    }

    @Test
    fun set_dark_persists_and_backups() = runBlocking {
        useCase.set(ThemePreference.Dark)
        assertThat(useCase.read()).isEqualTo(ThemePreference.Dark)
        assertThat(useCase.preference.value).isEqualTo(ThemePreference.Dark)
        assertThat(db.appMetaDao().get(AppMetaKeys.THEME_PREFERENCE)?.value).isEqualTo("dark")
        assertThat(ThemePreferenceCache.peek(context)).isEqualTo(ThemePreference.Dark)
        assertThat(backupCalls.get()).isEqualTo(1)
    }

    @Test
    fun hydrate_loads_stored_value() = runBlocking {
        useCase.set(ThemePreference.Light)
        val other = ThemePreferenceUseCase(
            context,
            db.appMetaDao(),
            MutatingRepository(
                db = db,
                backupScheduler = object : BackupScheduler {
                    override fun enqueueImmediate() = Unit
                },
                reminderScheduler = object : ReminderScheduler {
                    override suspend fun reconcileAll() = Unit
                    override suspend fun cancelFor(expenseId: String) = Unit
                },
            widgetSink = CarnetWidgetSink.Noop
        )
        )
        other.hydrate()
        assertThat(other.preference.value).isEqualTo(ThemePreference.Light)
        assertThat(ThemePreferenceCache.peek(context)).isEqualTo(ThemePreference.Light)
    }

    @Test
    fun peek_survives_without_room() {
        ThemePreferenceCache.write(context, ThemePreference.Dark)
        assertThat(ThemePreferenceCache.peek(context)).isEqualTo(ThemePreference.Dark)
    }
}
