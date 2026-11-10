package be.monmoisclair.app.ui.camera

/** Messages caméra partagés facture / courrier / scan. */
object CameraCaptureMessages {
    const val NEED_PERMISSION = "Autorisez la caméra pour prendre la photo."
    const val PERMANENTLY_DENIED =
        "Caméra refusée — ouvrez les réglages du téléphone."
    /** Annulation utilisateur (bouton retour / Annuler) — pas un refus d’autorisation. */
    const val CAPTURE_CANCELLED = "Photo annulée."
    const val CAPTURE_FAILED =
        "La photo n’a pas pu être prise. Réessayez, ou vérifiez les réglages du téléphone."
    const val SETTINGS_OPEN_FAILED =
        "Impossible d’ouvrir les réglages. Ouvrez ceux du téléphone puis autorisez la caméra."
    const val OPEN_SETTINGS = "Ouvrir les réglages"
}
