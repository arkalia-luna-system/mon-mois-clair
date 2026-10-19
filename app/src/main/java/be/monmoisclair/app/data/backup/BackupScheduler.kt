package be.monmoisclair.app.data.backup

/**
 * Planifie l'écriture du backup SAF après une mutation réussie.
 * Implémentation réelle à l'étape 1.2.
 */
fun interface BackupScheduler {
    fun enqueueImmediate()
}
