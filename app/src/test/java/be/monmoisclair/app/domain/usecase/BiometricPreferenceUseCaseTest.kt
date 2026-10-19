package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupScheduler
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.reminder.ReminderScheduler
import be.monmoisclair.app.data.repository.MutatingRepository
import be.monmoisclair.app.domain.model.AppMetaKeys
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class BiometricPreferenceUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: BiometricPreferenceUseCase
    private val backupCalls = AtomicInteger(0)

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
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
            }
        )
        useCase = BiometricPreferenceUseCase(db.appMetaDao(), mutating)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun default_is_disabled() = runBlocking {
        assertThat(useCase.isEnabled()).isFalse()
    }

    @Test
    fun setEnabled_true_persists_and_backups() = runBlocking {
        useCase.setEnabled(true)
        assertThat(useCase.isEnabled()).isTrue()
        assertThat(db.appMetaDao().get(AppMetaKeys.BIOMETRIC_ENABLED)?.value).isEqualTo("true")
        assertThat(backupCalls.get()).isEqualTo(1)
    }

    @Test
    fun setEnabled_false_clears_flag() = runBlocking {
        useCase.setEnabled(true)
        useCase.setEnabled(false)
        assertThat(useCase.isEnabled()).isFalse()
        assertThat(backupCalls.get()).isEqualTo(2)
    }
}
