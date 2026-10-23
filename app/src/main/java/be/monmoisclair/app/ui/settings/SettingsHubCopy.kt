package be.monmoisclair.app.ui.settings

/**
 * Libellés du hub Réglages.
 * Hub = 3 groupes (Données & sauvegarde / Confort / Aide) → sous-pages sujet.
 * Alertes / Aide / Restaurer restent accessibles depuis ici, pas depuis le Menu Accueil.
 * Profondeur max : hub → groupe → sujet (2 niveaux après hub).
 */
object SettingsHubCopy {
    const val TITLE = "Réglages"

    /** Pas « Compte » : l’app n’a pas de login / banque. */
    const val GROUP_ACCOUNT = "Données & sauvegarde"
    const val GROUP_COMFORT = "Confort"
    const val GROUP_HELP = "Aide"

    const val APPEARANCE = "Apparence"
    const val SAVINGS = "Mettre de côté"
    const val PRIVACY = "Confidentialité"
    const val ALERTS = "Alertes"
    const val DEADLINES = "Échéances hors ce mois"
    const val SUMMARY = "Préparer un résumé"
    const val BACKUP = "Sauvegarde"
    const val HELP = "Aide"

    /** Trois portes du hub (ordre fixe). */
    val hubGroups: List<String> = listOf(
        GROUP_ACCOUNT,
        GROUP_COMFORT,
        GROUP_HELP
    )

    fun comfortRows(): List<String> = listOf(APPEARANCE, ALERTS, DEADLINES)

    fun accountRows(showSummary: Boolean): List<String> =
        buildList {
            add(SAVINGS)
            add(PRIVACY)
            if (showSummary) add(SUMMARY)
            add(BACKUP)
        }

    /** Tous les sujets accessibles (charte) — hors groupes. */
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
