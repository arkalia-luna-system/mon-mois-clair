package be.monmoisclair.app.ui.restore

import be.monmoisclair.app.data.backup.RestoreResult

/** Messages UI pour les résultats de restauration (testable sans Android). */
object RestoreMessages {
    fun from(result: RestoreResult): String = when (result) {
        is RestoreResult.Success ->
            "Restauration réussie. Les données affichées viennent de la sauvegarde."
        is RestoreResult.NotConfigured ->
            "Aucun dossier de sauvegarde configuré. Choisissez d’abord un dossier Documents."
        is RestoreResult.UnsupportedSchema ->
            "Cette sauvegarde utilise une version non supportée (v${result.version})."
        is RestoreResult.InvalidChecksum ->
            "Sauvegarde corrompue ou modifiée (contrôle d’intégrité échoué)."
        is RestoreResult.IoError ->
            "Impossible de lire la sauvegarde : ${result.message}"
    }
}
