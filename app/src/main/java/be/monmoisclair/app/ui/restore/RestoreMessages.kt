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
        is RestoreResult.CleartextRejected -> CLEARTEXT_REJECTED
    }

    private fun successMessage(result: RestoreResult.Success): String {
        val base = "Restauration réussie. Les données affichées viennent de la sauvegarde."
        val photos = when {
            result.photosExpected <= 0 -> base
            result.photosWritten <= 0 ->
                "$base Les photos n’ont pas pu être remises : vérifiez le dossier Documents."
            result.photosWritten < result.photosExpected ->
                "$base ${result.photosWritten} photo(s) sur ${result.photosExpected} remises."
            result.photosExpected == 1 ->
                "$base 1 photo de facture remise."
            else ->
                "$base ${result.photosWritten} photos de factures remises."
        }
        return if (result.cleartextGrace) {
            "$photos $CLEARTEXT_GRACE_WARNING"
        } else {
            photos
        }
    }

    /** Refus par défaut — cocher « Ancien format (ZIP/JSON clair) » pour forcer. */
    const val CLEARTEXT_REJECTED =
        "Cette copie est en clair (ancienne version). " +
            "Cochez « Ancien format (ZIP/JSON clair) » pour l’accepter une fois, " +
            "puis faites tout de suite une nouvelle copie chiffrée. " +
            "Le mois en cours n’a pas été modifié."

    /** Warning fort — grace pré-6.81 acceptée via opt-in, à remplacer par une copie .mmc. */
    const val CLEARTEXT_GRACE_WARNING =
        "Attention : cette copie était en clair (ancienne version). " +
            "Faites tout de suite une nouvelle copie chiffrée (Réglages → Envoyer une copie) " +
            "et jetez les vieux ZIP/JSON sur Drive ou WhatsApp."
}
