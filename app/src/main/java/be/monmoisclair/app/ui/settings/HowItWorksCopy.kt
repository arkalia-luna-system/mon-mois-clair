package be.monmoisclair.app.ui.settings

/**
 * Aide « Comment ça marche » — textes figés, testables, ton familial 60+.
 * Affiché depuis Réglages (contact = Menu Accueil uniquement).
 * P1-U4 : FAIT + Sauvegarde en tête (1ʳᵉ panique / geste fort).
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
            body = "Réglages → Sauvegarde : écrivez le code une fois sur papier. " +
                "N’envoyez jamais le code avec la copie. " +
                "Nouveau téléphone : Restaurer, puis ce code. " +
                "Les vieilles copies non protégées ne s’ouvrent plus — à jeter."
        ),
        Section(
            title = "Le chiffre en haut",
            body = "« Reste après factures » = revenus − factures du mois — pas le solde bancaire. " +
                "« Factures ouvertes » = encore dû. Dans Détails, « Après les FAIT » = " +
                "seulement les cochées. Pas de report auto : un revenu « Reste du mois passé », une fois."
        ),
        Section(
            title = "Courses",
            body = "À côté de « Mettre de côté » sous le Reste (›) : courses / pharmacie. " +
                "Le gros Reste ne bouge pas ; le détail « il reste » est dans la feuille."
        ),
        Section(
            title = "Mettre de côté",
            body = "À côté de Courses sous le Reste (›). " +
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
                "Menu → Dossiers. Résumé texte/PDF pour créancier."
        ),
        Section(
            title = "Facture papier",
            body = "« Scanner une facture » lit le QR belge. « Copier » pour la banque. " +
                "Photo et PDF à côté. « Lire la communication sur la photo » = " +
                "communication seulement — jamais le montant. Eau / énergie : souvent " +
                "une nouvelle +++ chaque mois (loyer ou prélèvement : la ref peut rester)."
        ),
        Section(
            title = "Sur une année",
            body = "« 1× par an » / « tous les 3 mois » : réapparaissent le bon mois. " +
                "Aperçu : calendrier Accueil ou Menu → À venir. " +
                "Hors ce mois : Réglages → Données → Échéances hors ce mois."
        ),
        Section(
            title = "Résumé du mois",
            body = "Réglages → Données → Préparer un résumé, ou Menu → Dossiers. " +
                "Texte ou PDF : Reste, À payer, FAIT. À envoyer à qui vous voulez — " +
                "rien ne quitte le téléphone tant que vous ne partagez pas."
        ),
        Section(
            title = "Alertes",
            body = "3 j avant à 10 h, le jour à 9 h, puis chaque matin à 10 h si impayé. " +
                "Rien ne sonne : Réglages → Alertes. " +
                DeviceCareCopy.HINT
        ),
        Section(
            title = "Empreinte",
            body = "Optionnel : cochez pour cacher le mois aux autres. " +
                "Les alertes masquent alors les montants."
        )
    )
}
