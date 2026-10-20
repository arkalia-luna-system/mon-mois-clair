package be.monmoisclair.app.domain.usecase

/** Phrases UI pour l’échec d’envoi d’une copie (détail technique = log seulement). */
object ShareBackupMessages {
    const val FAILED =
        "Partage impossible. Réessayez, ou copiez le dossier Documents à la main."

    const val FOLDER_REQUIRED =
        "Choisissez d’abord le dossier de sauvegarde (Documents)."
}
