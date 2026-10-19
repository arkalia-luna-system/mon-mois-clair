package be.monmoisclair.app.domain.model

/**
 * Santé du pipeline d'auto-backup SAF. Remonté dans AppMeta pour alerter l'UI.
 */
enum class BackupHealth {
    NOT_CONFIGURED,
    OK,
    BROKEN
}
