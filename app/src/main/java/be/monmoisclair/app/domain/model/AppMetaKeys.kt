package be.monmoisclair.app.domain.model

object AppMetaKeys {
    const val SCHEMA_VERSION = "schemaVersion"
    const val SAF_TREE_URI = "safTreeUri"
    const val ONBOARDING_DONE = "onboardingDone"
    const val BIOMETRIC_ENABLED = "biometricEnabled"
    const val LAST_BACKUP_AT_EPOCH_MS = "lastBackupAtEpochMs"
    const val LAST_BACKUP_CHECKSUM_SHA256 = "lastBackupChecksumSha256"
    const val BACKUP_HEALTH = "backupHealth"

    const val CURRENT_SCHEMA_VERSION = "4"
}
