package be.monmoisclair.app.ui.settings

/**
 * Aide « Comment ça marche » — textes figés, testables, ton familial 60+.
 * Affiché depuis Réglages (contact = Menu Accueil uniquement).
 * 7.08 : corps courts ; Courses/épargne = Accueil (à côté du Reste) ; Dossiers conditionnel.
 */
object HowItWorksCopy {
    const val TITLE = "Comment ça marche"

    data class Section(val title: String, val body: String)

    val sections: List<Section> = listOf(
        Section(
            title = "Marquer FAIT",
            body = "Payé : « FAIT ✓ », puis « FAIT le … ». « Remettre » si erreur. " +
                "Prélèvement : cochez quand vous voyez le débit, pas le jour prévu. " +
                "Autre montant ce mois : ouvrez la ligne — seul ce mois change, " +
                "sauf « Aussi les mois suivants »."
        ),
        Section(
            title = "Sauvegarde",
            body = "Réglages → Sauvegarde (ou « Code et copie » en tête) : code une fois sur papier. " +
                "N’envoyez jamais le code avec la copie. " +
                "Nouveau téléphone : Restaurer, puis ce code. " +
                "Les vieilles copies non protégées ne s’ouvrent plus — à jeter."
        ),
        Section(
            title = "Le chiffre en haut",
            body = "« Reste » = revenus − factures du mois — pas le solde bancaire. " +
                "Sous la liste : « Encore à payer » = encore dû. " +
                "Dans Détails, « Après les FAIT » = seulement les cochées. " +
                "Pas de report auto : un revenu « Reste du mois passé », une fois."
        ),
        Section(
            title = "Courses",
            body = "Accueil, à côté du Reste : icône chariot Courses — courses / pharmacie. " +
                "Le gros Reste ne bouge pas ; le détail « il reste » est dans la feuille."
        ),
        Section(
            title = "Mettre de côté",
            body = "Accueil, à côté du Reste (sous Courses) : icône tirelire Mettre de côté. " +
                "Jour et retrait : Réglages → Données → Jour du Mettre de côté. " +
                "FAIT = virement fait. Pas d’argent dans l’app."
        ),
        Section(
            title = "En retard",
            body = "Date passée et pas FAIT : pastille brique. Rappel chaque jour à 10 h jusqu’au FAIT. " +
                "« Mettre de côté » n’est jamais en retard — c’est un geste volontaire."
        ),
        Section(
            title = "Retard ou accord",
            body = "Ce n’est pas la pastille « En retard ». C’est un plan de remboursement " +
                "(à qui, pour quoi, reste dû) : pastille « Accord » à l’accueil, " +
                "sans le total de la dette. Chaque FAIT baisse le reste. " +
                "Menu → Dossiers (quand un accord ou un courrier existe). " +
                "Résumé texte/PDF pour créancier."
        ),
        Section(
            title = "Facture papier",
            body = "« Scanner une facture » lit le QR belge. « Copier » pour la banque. " +
                "Photo et PDF à côté. « Lire la communication sur la photo » = " +
                "communication seulement — jamais le montant. Eau / énergie : souvent " +
                "une nouvelle +++ chaque mois."
        ),
        Section(
            title = "Sur une année",
            body = "« 1× par an » / « tous les 3 mois » : réapparaissent le bon mois. " +
                "Aperçu : calendrier Accueil ou Menu → À venir. " +
                "Hors ce mois : Réglages → Données → Échéances hors ce mois."
        ),
        Section(
            title = "Résumé du mois",
            body = "Réglages → Données → Préparer un résumé. " +
                "Si un accord ou un courrier existe : aussi Menu → Dossiers. " +
                "Texte ou PDF : Reste, À payer, FAIT. " +
                "Rien ne quitte le téléphone tant que vous ne partagez pas."
        ),
        Section(
            title = "Alertes",
            body = "3 j avant à 10 h, le jour à 9 h, puis chaque matin à 10 h si impayé. " +
                "Rien ne sonne : Réglages → Confort → Alertes. " +
                DeviceCareCopy.HINT
        ),
        Section(
            title = "Empreinte",
            body = "Optionnel : cochez pour cacher le mois aux autres. " +
                "Les alertes masquent alors les montants."
        )
    )
}
