package be.monmoisclair.app.ui.home

/**
 * Nouveautés à l’ouverture — une entrée par version visible (versionCode Play).
 * Langage simple ; 3–6 puces au total après fusion.
 */
object WhatsNewCopy {
    const val TITLE = "Nouveautés"
    const val DISMISS = "J’ai compris"
    const val MAX_BULLETS = 6

    data class Release(val versionCode: Int, val bullets: List<String>)

    /** Ordre croissant versionCode. */
    val releases: List<Release> = listOf(
        Release(
            versionCode = 146,
            bullets = listOf(
                "Bandeau « Pour aujourd’hui » : retards et échéance du jour, avec « Voir » sur la ligne.",
                "FAIT : un message vous laisse « Annuler » si vous vous êtes trompé·e.",
                "Dossiers : filtres Tous, « À traiter » ou Ce mois pour ne pas vous noyer.",
                "Résumé texte ou PDF : une rubrique « Aujourd’hui » pour partager avec la famille."
            )
        ),
        Release(
            versionCode = 147,
            bullets = listOf(
                "Cet écran résume les changements après chaque mise à jour — une seule fois.",
                "Aide mise à jour : même case « Appliquer aussi aux mois suivants », anti-doublon, « Pour aujourd’hui ».",
                "Petit rappel sauvegarde si vous n’en avez jamais fait — doux, sans insister."
            )
        )
    )

    data class Payload(val bullets: List<String>)

    /**
     * @param lastSeenVersionCode 0 = jamais vu (première ouverture ou ancienne install).
     * @param currentVersionCode version installée (BuildConfig / PackageManager).
     */
    fun payload(lastSeenVersionCode: Int, currentVersionCode: Int): Payload? {
        if (currentVersionCode <= 0) return null
        if (lastSeenVersionCode >= currentVersionCode) return null
        val bullets = releases
            .filter { it.versionCode > lastSeenVersionCode && it.versionCode <= currentVersionCode }
            .flatMap { it.bullets }
            .distinct()
            .take(MAX_BULLETS)
        if (bullets.isEmpty()) return null
        return Payload(bullets)
    }

    const val POST_WHATS_NEW_HELP_TIP =
        "Astuce : Menu → Réglages → Comment ça marche pour le mode d’emploi complet."

    const val BACKUP_NUDGE_TITLE = "Pensez à une copie"
    const val BACKUP_NUDGE_BODY =
        "Vous n’avez pas encore fait de sauvegarde. " +
            "Réglages → Sauvegarde : notez le code sur papier, une fois."
    const val BACKUP_NUDGE_LATER = "Plus tard"
    const val BACKUP_NUDGE_OPEN = "Ouvrir Sauvegarde"
}
