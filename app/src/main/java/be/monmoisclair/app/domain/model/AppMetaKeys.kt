package be.monmoisclair.app.domain.model

object AppMetaKeys {
    const val SCHEMA_VERSION = "schemaVersion"
    const val SAF_TREE_URI = "safTreeUri"
    const val ONBOARDING_DONE = "onboardingDone"
    const val BIOMETRIC_ENABLED = "biometricEnabled"
    /** Clair / sombre / système — voir [ThemePreference]. */
    const val THEME_PREFERENCE = "themePreference"
    const val LAST_BACKUP_AT_EPOCH_MS = "lastBackupAtEpochMs"
    const val LAST_BACKUP_CHECKSUM_SHA256 = "lastBackupChecksumSha256"
    const val BACKUP_HEALTH = "backupHealth"
    /** Lot E — id du modèle « Mettre de côté ». Vide = pas activé. */
    const val SAVINGS_TEMPLATE_ID = "savingsTemplateId"

    /**
     * Ids d’instances volontairement retirées « ce mois seulement »
     * (évite la résurrection via syncMissingTemplates). Séparateur `\n`.
     */
    const val SUPPRESSED_INSTANCE_IDS = "suppressedInstanceIds"

    const val CURRENT_SCHEMA_VERSION = "8"
}
