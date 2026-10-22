package be.monmoisclair.app.ui.settings

/**
 * Aide « Comment ça marche » — textes figés, testables, ton familial.
 * Affiché depuis Réglages (Menu = outils + lien calendrier ; À venir ; dossiers).
 */
object HowItWorksCopy {
    const val TITLE = "Comment ça marche"

    data class Section(val title: String, val body: String)

    val sections: List<Section> = listOf(
        Section(
            title = "Le chiffre en haut",
            body = "« Reste après factures » = revenus − factures du mois (FAIT ou non) — pas le solde bancaire. " +
                "« À payer » = encore à régler. « Après les FAIT » (brun) = seulement les cochées. " +
                "Pas de report auto : un revenu « Reste du mois passé », une fois."
        ),
        Section(
            title = "Courses",
            body = "Ligne Courses sous le Reste (›) : courses / pharmacie. " +
                "Le gros Reste ne bouge pas ; « Après courses » montre le solde (brique s’il est négatif)."
        ),
        Section(
            title = "Marquer FAIT",
            body = "Payé (ou prélèvement vérifié) : « FAIT ✓ », puis « FAIT le … ». « Remettre » si erreur. " +
                "Montant différent ce mois : ouvrez la ligne — seul ce mois change, " +
                "sauf si « Aussi les mois suivants » est coché."
        ),
        Section(
            title = "En retard",
            body = "Date passée et pas encore FAIT : badge « En retard » (brique). " +
                "Rappel chaque jour à 10 h jusqu’au FAIT."
        ),
        Section(
            title = "Alertes",
            body = "Par facture : 3 jours avant à 10 h, le jour à 9 h, " +
                "puis chaque matin à 10 h tant que ce n’est pas payé. " +
                "Si rien ne sonne : Réglages → Alertes. " +
                DeviceCareCopy.HINT
        ),
        Section(
            title = "Facture papier",
            body = "À l’ajout, « Scanner une facture » lit le QR belge (nom, montant, référence). " +
                "« Copier » pour la banque. Photo et « Joindre un PDF » à côté " +
                "(le PDF ne remplace pas la photo). " +
                "« Lire +++ sur la photo » lit seulement la communication — jamais le montant."
        ),
        Section(
            title = "Retard ou accord",
            body = "« C’est un retard ou un accord ? » pour un plan (à qui, pour quoi, reste dû). " +
                "L’accueil montre le versement et une pastille « Accord », pas le total de la dette. " +
                "Chaque FAIT baisse le reste. S’il y a un accord : Menu → « Mes dossiers ». " +
                "Résumé (texte ou PDF) pour créancier ou conseiller, pas tout le monde."
        ),
        Section(
            title = "Mettre de côté",
            body = "Réglages → facture mensuelle vers un autre compte. " +
                "FAIT = virement fait. Pas d’argent dans l’app."
        ),
        Section(
            title = "Sur une année",
            body = "« 1× par an » ou « tous les 3 mois » : créées une fois, réapparaissent le bon mois. " +
                "Aperçu : flèche ›, calendrier Accueil, ou Menu → « À venir ». " +
                "Hors ce mois : Réglages → « Échéances hors ce mois ». " +
                "Fréquence erronée : ouvrez la ligne et changez-la."
        ),
        Section(
            title = "Sauvegarde",
            body = "Documents (3 versions) + « Envoyer une copie ». " +
                "Nouveau téléphone : Restaurer."
        ),
        Section(
            title = "Empreinte (optionnel)",
            body = "Optionnel : cochez pour cacher le mois aux autres."
        )
    )
}
