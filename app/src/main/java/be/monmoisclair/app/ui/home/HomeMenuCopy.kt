package be.monmoisclair.app.ui.home

/**
 * Libellés fixes du menu Accueil.
 * Les lignes conditionnelles (Alertes / Dossiers / Mois en cours) restent gérées dans l'écran.
 */
object HomeMenuCopy {
    const val TITLE = "Menu"
    const val SETTINGS = "Réglages"
    const val UPCOMING = "Mois à venir"
    const val PICK_MONTH = "Choisir un mois"
    const val HELP = "Aide / Comment ça marche"
    const val SUPPORT = "Un souci ? Écrire à Arkalia Système"
    const val SUPPORT_A11Y = "Écrire à Arkalia Système — bug, idée ou question"

    /** Ordre des accès fixes avant le lien support. */
    val visibleRows: List<String> = listOf(
        SETTINGS,
        UPCOMING,
        PICK_MONTH,
        HELP
    )
}
