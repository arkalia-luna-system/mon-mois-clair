package be.monmoisclair.app.ui.settings

/**
 * Libellés du hub Réglages.
 * Hub = 3 groupes (Données & sauvegarde / Confort / Aide) → sous-pages sujet.
 * Alertes / Aide / Restaurer restent accessibles depuis ici, pas depuis le Menu Accueil.
 * Profondeur max : hub → groupe → sujet (2 niveaux après hub).
 *
 * Échéances = métier (Données), pas Confort.
 * « Jour du Mettre de côté » = config (jour / retrait) ; montant = Accueil.
 */
object SettingsHubCopy {
    const val TITLE = "Réglages"

    /** Pas « Compte » : l’app n’a pas de login / banque. */
    const val GROUP_ACCOUNT = "Données & sauvegarde"
    const val GROUP_COMFORT = "Confort"
    const val GROUP_HELP = "Comment ça marche"

    const val APPEARANCE = "Apparence"
    /** Page titre — même nom produit qu’Accueil. */
    const val SAVINGS = "Mettre de côté"
    /**
     * Ligne hub / groupe : précise le surplus vs sticky Accueil (montant).
     * Valeur réelle = jour du mois + retrait.
     */
    const val SAVINGS_ROW = "Jour du Mettre de côté"
    const val PRIVACY = "Confidentialité"
    const val ALERTS = "Alertes"
    const val DEADLINES = "Échéances hors ce mois"
    const val SUMMARY = "Préparer un résumé"
    const val BACKUP = "Sauvegarde"
    /** P0-U1 : raccourci hub — code vital sans passer par le groupe. */
    const val BACKUP_SHORTCUT = "Code et copie"
    const val HELP = "Comment ça marche"

    /** Trois portes du hub (ordre fixe). */
    val hubGroups: List<String> = listOf(
        GROUP_ACCOUNT,
        GROUP_COMFORT,
        GROUP_HELP
    )

    fun comfortRows(): List<String> = listOf(APPEARANCE, ALERTS)

    fun accountRows(showSummary: Boolean): List<String> =
        buildList {
            add(BACKUP)
            add(DEADLINES)
            add(SAVINGS_ROW)
            add(PRIVACY)
            if (showSummary) add(SUMMARY)
        }

    /** Tous les sujets accessibles (charte) — hors groupes. */
    val alwaysVisibleRows: List<String> = listOf(
        APPEARANCE,
        ALERTS,
        BACKUP,
        DEADLINES,
        SAVINGS_ROW,
        PRIVACY,
        HELP
    )

    fun hubRows(showSummary: Boolean): List<String> =
        if (showSummary) {
            alwaysVisibleRows.toMutableList().also {
                it.add(it.indexOf(PRIVACY) + 1, SUMMARY)
            }
        } else {
            alwaysVisibleRows
        }
}
