package be.monmoisclair.app.ui.settings

import be.monmoisclair.app.ui.clipboard.ClipboardSecurityCopy

/**
 * Aide « Comment ça marche » — textes figés, testables, ton senior.
 * Accessible depuis le Menu Accueil et depuis Réglages.
 */
object HowItWorksCopy {
    const val TITLE = "Comment ça marche"

    data class Section(val title: String, val body: String)

    val sections: List<Section> = listOf(
        Section(
            title = "Première ouverture",
            body = "Au début, choisissez un dossier dans Documents. " +
                "Les copies y sont gardées. " +
                "Puis vérifiez les alertes. " +
                "Vous pouvez continuer même si tout n’est pas encore prêt."
        ),
        Section(
            title = "Noter une facture",
            body = "Depuis l’accueil, touchez +. " +
                "Notez le nom, le jour et le montant. " +
                "Ajoutez la communication si vous l’avez. " +
                "Touchez Enregistrer. " +
                "Quand la case est visible, cochez « Appliquer aussi aux mois suivants » " +
                "seulement si le changement vaut aussi pour les prochains mois."
        ),
        Section(
            title = "Marquer FAIT",
            body = "Quand c’est payé, touchez FAIT. " +
                "Pour un prélèvement, attendez de voir le débit. " +
                "Si vous vous trompez, utilisez Annuler ou Remettre. " +
                "Un doublon ressemblant dans le mois est signalé."
        ),
        Section(
            title = "Sauvegarde",
            body = "Réglages → Code et copie. " +
                "Notez le code une fois sur papier. " +
                "Choisissez le dossier si besoin. " +
                "Puis envoyez une copie. " +
                "N’envoyez jamais le code avec le fichier. " +
                "Nouveau téléphone : Restaurer une sauvegarde, puis entrer ce code."
        ),
        Section(
            title = "Facture papier",
            body = "« Scanner une facture (QR) » lit le QR belge. " +
                "« Photo de la facture papier » garde l’image. " +
                "« Lire la communication sur la photo » lit seulement la communication. " +
                "« Joindre un PDF » ajoute un courrier à côté. " +
                "Rien n’est gardé tant que vous n’avez pas touché Enregistrer."
        ),
        Section(
            title = "Copier pour la banque",
            body = ClipboardSecurityCopy.HOW_IT_WORKS_BODY
        ),
        Section(
            title = "Plan de remboursement",
            body = "Si vous notez un reste dû, la facture devient un accord. " +
                "La pastille « Accord » apparaît sur l’accueil. " +
                "Menu → Dossiers ouvre les accords et les courriers. " +
                "Filtres : Tous, À traiter, Ce mois."
        ),
        Section(
            title = "Résumé du mois",
            body = "Réglages → Données & sauvegarde → Préparer un résumé. " +
                "S’il y a un accord ou un courrier, vous pouvez aussi passer par Menu → Dossiers. " +
                "Le texte ou le PDF reprend le Reste, À payer et FAIT. " +
                "Rien ne part sans votre partage."
        ),
        Section(
            title = "Sur une année",
            body = "« 1× par an » ou « tous les 3 mois » revient le bon mois. " +
                "Pour voir plus loin, ouvrez À venir ou Choisir un mois. " +
                "Les lignes hors ce mois se règlent dans " +
                "Réglages → Données & sauvegarde → Échéances hors ce mois."
        ),
        Section(
            title = "Le chiffre en haut",
            body = "« Reste » = revenus moins factures du mois. " +
                "Ce n’est pas le solde bancaire. " +
                "Plus bas, « Encore à payer » montre ce qu’il reste à régler. " +
                "Dans Détails, « Après les FAIT » montre seulement ce qui est coché."
        ),
        Section(
            title = "Courses",
            body = "À côté du Reste, touchez Courses pour noter courses ou pharmacie. " +
                "Le grand Reste du haut ne bouge pas. " +
                "Le détail « il reste » se voit dans la feuille."
        ),
        Section(
            title = "Mettre de côté",
            body = "À côté du Reste, sous Courses, touchez Mettre de côté. " +
                "Choisissez le jour dans Réglages → Données & sauvegarde → Jour du Mettre de côté. " +
                "FAIT veut dire : virement fait. " +
                "L’app ne garde pas d’argent."
        ),
        Section(
            title = "En retard",
            body = "Quand la date est passée et qu’il n’y a pas FAIT, la pastille devient brique. " +
                "Le rappel revient chaque matin à 10 h. " +
                "Mettre de côté n’est jamais en retard."
        ),
        Section(
            title = "Pour aujourd’hui",
            body = "Sur le mois en cours, le bandeau « Pour aujourd’hui » montre les retards " +
                "et l’échéance du jour. " +
                "Touchez « Voir » pour aller à la ligne."
        ),
        Section(
            title = "Nouveautés",
            body = "Après une mise à jour, « Nouveautés » peut s’ouvrir une seule fois. " +
                "Touchez « J’ai compris » ou « Passer ». " +
                "Les anciennes nouveautés ne reviennent pas."
        ),
        Section(
            title = "Alertes",
            body = "Rappel 3 jours avant à 10 h. " +
                "Rappel le jour même à 9 h. " +
                "Puis rappel chaque matin à 10 h si ce n’est pas payé. " +
                "Rien ne sonne : Réglages → Confort → Alertes. " +
                DeviceCareCopy.HINT
        ),
        Section(
            title = "Empreinte",
            body = "Optionnel. " +
                "Activez l’empreinte ou le code du téléphone pour cacher le mois aux autres. " +
                "Les alertes masquent aussi les montants."
        )
    )
}
