package be.monmoisclair.app.ui.restore

import be.monmoisclair.app.data.backup.RestoreResult

/** Messages UI pour les résultats de restauration (testable sans Android). */
object RestoreMessages {
    fun from(result: RestoreResult): String = when (result) {
        is RestoreResult.Success -> successMessage(result)
        is RestoreResult.NotConfigured ->
            "Aucun dossier de sauvegarde configuré. Choisissez d’abord un dossier Documents."
        is RestoreResult.UnsupportedSchema ->
            "Cette copie ne peut pas être lue par cette app. Le mois en cours n’a pas été modifié."
        is RestoreResult.InvalidChecksum ->
            "Cette copie est abîmée ou a été modifiée. Le mois en cours n’a pas été modifié."
        is RestoreResult.IoError ->
            "Impossible de lire cette sauvegarde. Le mois en cours n’a pas été modifié."
        is RestoreResult.BadShareCode -> result.message
    }

    private fun successMessage(result: RestoreResult.Success): String {
        val base = "Restauration réussie. Les données affichées viennent de la sauvegarde."
        if (result.photosExpected <= 0) return base
        return when {
            result.photosWritten <= 0 ->
                "$base Les photos n’ont pas pu être remises : vérifiez le dossier Documents."
            result.photosWritten < result.photosExpected ->
                "$base ${result.photosWritten} photo(s) sur ${result.photosExpected} remises."
            result.photosExpected == 1 ->
                "$base 1 photo de facture remise."
            else ->
                "$base ${result.photosWritten} photos de factures remises."
        }
    }
}
