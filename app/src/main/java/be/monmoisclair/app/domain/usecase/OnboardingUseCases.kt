package be.monmoisclair.app.domain.usecase

import android.net.Uri
import be.monmoisclair.app.data.backup.BackupCoordinator
import be.monmoisclair.app.data.backup.SafBackupStore
import be.monmoisclair.app.data.db.dao.AppMetaDao
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import be.monmoisclair.app.domain.model.AppMetaKeys
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveOnboardingStateUseCase @Inject constructor(
    private val appMetaDao: AppMetaDao,
    private val safBackupStore: SafBackupStore
) {
    suspend fun isOnboardingDone(): Boolean =
        appMetaDao.get(AppMetaKeys.ONBOARDING_DONE)?.value == "true"

    suspend fun hasSafFolder(): Boolean = safBackupStore.hasPersistedTree()

    suspend fun startDestination(): StartDestination {
        if (!hasSafFolder()) return StartDestination.SafSetup
        if (!isOnboardingDone()) return StartDestination.PermissionsSetup
        return StartDestination.Home
    }
}

enum class StartDestination {
    SafSetup,
    PermissionsSetup,
    Home
}

@Singleton
class PersistSafFolderUseCase @Inject constructor(
    private val safBackupStore: SafBackupStore,
    private val backupCoordinator: BackupCoordinator
) {
    sealed class Result {
        data object Success : Result()
        data class Failed(val message: String) : Result()
    }

    suspend operator fun invoke(uri: Uri): Result {
        return try {
            safBackupStore.persistTreeUri(uri)
            // Premier snapshot (même vide) pour valider l'écriture Documents.
            when (val backup = backupCoordinator.runAutoBackup()) {
                is BackupCoordinator.Result.Success -> Result.Success
                // Après persistTreeUri, un skip est anormal → ne pas faire croire au succès.
                BackupCoordinator.Result.SkippedNotConfigured ->
                    Result.Failed("Dossier enregistré mais backup initial non exécuté")
                is BackupCoordinator.Result.Failed ->
                    Result.Failed(backup.failure.message ?: "Backup initial échoué")
            }
        } catch (e: Exception) {
            Result.Failed(e.message ?: "Impossible d'enregistrer le dossier")
        }
    }
}

@Singleton
class CompleteOnboardingUseCase @Inject constructor(
    private val appMetaDao: AppMetaDao,
    private val safBackupStore: SafBackupStore
) {
    sealed class Result {
        data object Success : Result()
        data object SafMissing : Result()
    }

    suspend operator fun invoke(): Result {
        if (!safBackupStore.hasPersistedTree()) return Result.SafMissing
        appMetaDao.upsert(AppMetaEntity(AppMetaKeys.ONBOARDING_DONE, "true"))
        return Result.Success
    }
}
