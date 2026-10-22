package be.monmoisclair.app.ui.settings

/**
 * Libellés du hub Réglages (sous-menus). Une entrée = un sujet.
 * Alertes / Aide / Restaurer restent accessibles depuis ici, pas depuis le Menu Accueil.
 */
object SettingsHubCopy {
    const val TITLE = "Réglages"

    const val APPEARANCE = "Apparence"
    const val SAVINGS = "Mettre de côté"
    const val PRIVACY = "Confidentialité"
    const val ALERTS = "Alertes"
    const val DEADLINES = "Échéances"
    const val SUMMARY = "Résumé à montrer"
    const val BACKUP = "Sauvegarde"
    const val HELP = "Aide"

    /** Ordre fixe du hub (sans résumé conditionnel). */
    val alwaysVisibleRows: List<String> = listOf(
        APPEARANCE,
        SAVINGS,
        PRIVACY,
        ALERTS,
        DEADLINES,
        BACKUP,
        HELP
    )

    fun hubRows(showSummary: Boolean): List<String> =
        if (showSummary) {
            listOf(
                APPEARANCE,
                SAVINGS,
                PRIVACY,
                ALERTS,
                DEADLINES,
                SUMMARY,
                BACKUP,
                HELP
            )
        } else {
            alwaysVisibleRows
        }
}
