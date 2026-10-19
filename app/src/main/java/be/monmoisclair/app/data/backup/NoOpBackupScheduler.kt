package be.monmoisclair.app.data.backup

import javax.inject.Inject
import javax.inject.Singleton

/** Placeholder jusqu'à l'étape 1.2 (SAF). */
@Singleton
class NoOpBackupScheduler @Inject constructor() : BackupScheduler {
    override fun enqueueImmediate() = Unit
}
