package be.monmoisclair.app.ui.entry

/** Messages UI partagés Add / Edit (pièces jointes, QR). */
object EntryFormMessages {
    const val PHOTO_DECODE_ERROR = "La photo n’a pas pu être lue. Réessayez."
    const val CAMERA_CAPTURE_CANCELLED =
        be.monmoisclair.app.ui.camera.CameraCaptureMessages.CAPTURE_CANCELLED
    const val CAMERA_CAPTURE_FAILED =
        be.monmoisclair.app.ui.camera.CameraCaptureMessages.CAPTURE_FAILED
    const val PDF_READ_ERROR_DEFAULT = "Ce PDF n’a pas pu être lu. Réessayez."
    const val INVALID_EPC_QR = "Ce QR n’est pas une facture belge."
    const val EPC_IBAN_HINT =
        "IBAN lu — ouvrez « Plan de remboursement » pour le garder."
    const val DOSSIER_SECTION_TITLE = "Plan de remboursement"
}
