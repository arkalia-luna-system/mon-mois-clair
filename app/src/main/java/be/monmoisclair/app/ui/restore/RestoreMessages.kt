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
        is RestoreResult.NotMmcBackup -> NOT_MMC_BACKUP
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

    /**
     * Refus des copies non protégées (grâce close depuis 6.99).
     * Demander une nouvelle copie protégée avec code noté à part.
     */
    const val CLEARTEXT_REJECTED =
        "Cette copie est une ancienne copie non protégée. " +
            "Elle n’est plus acceptée. Sur l’ancien téléphone, " +
            "faites une nouvelle copie (Réglages → Sauvegarde), notez le code à part, " +
            "puis restaurez cette copie ici. Le mois en cours n’a pas été modifié."

    /** JPEG / PDF / autre fichier qui n’est pas une copie Mon Mois Clair. */
    const val NOT_MMC_BACKUP =
        "Ce fichier n’est pas une copie Mon Mois Clair. " +
            "Choisissez le fichier de sauvegarde (Réglages → Sauvegarde), " +
            "pas une photo ni un PDF. Le mois en cours n’a pas été modifié."

    /**
     * Warning résiduel si une restauration claire passait encore (tests / chemin interne).
     * Produit : grâce close — ce texte ne doit plus apparaître en usage normal.
     */
    const val CLEARTEXT_GRACE_WARNING =
        "Attention : cette copie était une ancienne copie non protégée. " +
            "Faites tout de suite une nouvelle copie (Réglages → Sauvegarde), " +
            "notez le code à part, et jetez l’ancienne sur Drive ou WhatsApp."

    /** Libellé checkbox — masqué si [be.monmoisclair.app.data.backup.CleartextLegacyGrace.OPT_IN_AVAILABLE] = false. */
    const val CLEARTEXT_GRACE_CHECKBOX =
        "Vieille copie non protégée — à ouvrir une seule fois, puis à refaire"
}
