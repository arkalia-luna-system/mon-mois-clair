package be.monmoisclair.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import be.monmoisclair.app.data.backup.BackupHealthStore
import be.monmoisclair.app.data.backup.BackupJsonCodec
import be.monmoisclair.app.data.backup.BackupRotator
import be.monmoisclair.app.data.backup.SafBackupStore
import be.monmoisclair.app.data.db.AppDatabase
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.domain.model.AppMetaKeys
import be.monmoisclair.app.domain.model.BackupHealth
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OnboardingUseCasesTest {

    private lateinit var db: AppDatabase
    private lateinit var observe: ObserveOnboardingStateUseCase
    private lateinit var complete: CompleteOnboardingUseCase
    private lateinit var safStore: SafBackupStore

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val health = BackupHealthStore(db.appMetaDao())
        safStore = SafBackupStore(
            context = context,
            appMetaDao = db.appMetaDao(),
            codec = BackupJsonCodec(),
            rotator = BackupRotator(),
            healthStore = health
        )
        observe = ObserveOnboardingStateUseCase(db.appMetaDao(), safStore)
        complete = CompleteOnboardingUseCase(db.appMetaDao(), safStore)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun startDestination_withoutSaf_isSafSetup() = runBlocking {
        assertThat(observe.startDestination()).isEqualTo(StartDestination.SafSetup)
        assertThat(observe.hasSafFolder()).isFalse()
    }

    @Test
    fun completeOnboarding_withoutTree_returnsSafMissing_andDoesNotSetFlag() = runBlocking {
        val result = complete()
        assertThat(result).isEqualTo(CompleteOnboardingUseCase.Result.SafMissing)
        assertThat(db.appMetaDao().get(AppMetaKeys.ONBOARDING_DONE)).isNull()
        assertThat(observe.isOnboardingDone()).isFalse()
    }

    @Test
    fun isOnboardingDone_readsMetaFlag() = runBlocking {
        assertThat(observe.isOnboardingDone()).isFalse()
        db.appMetaDao().upsert(AppMetaEntity(AppMetaKeys.ONBOARDING_DONE, "true"))
        assertThat(observe.isOnboardingDone()).isTrue()
    }

    @Test
    fun startDestination_prefersSafSetup_whenFolderMissingEvenIfDoneFlagSet() = runBlocking {
        db.appMetaDao().upsert(AppMetaEntity(AppMetaKeys.ONBOARDING_DONE, "true"))
        db.appMetaDao().upsert(
            AppMetaEntity(AppMetaKeys.BACKUP_HEALTH, BackupHealth.OK.name)
        )
        // Sans arbre SAF réel, on force le retour à la config dossier.
        assertThat(observe.startDestination()).isEqualTo(StartDestination.SafSetup)
    }
}
