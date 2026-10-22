package be.monmoisclair.app.ui.settings

/**
 * Aide « Comment ça marche » — textes figés, testables, ton familial.
 * Affiché depuis Réglages (Menu = calendrier, À venir, Mes dossiers, Réglages).
 */
object HowItWorksCopy {
    const val TITLE = "Comment ça marche"

    data class Section(val title: String, val body: String)

    val sections: List<Section> = listOf(
        Section(
            title = "Le chiffre en haut",
            body = "Le gros chiffre, c’est le « Reste après factures » : revenus moins toutes les factures " +
                "(payées ou non). « À payer » = encore à régler. « Après les FAIT » (brun) = seulement " +
                "les factures déjà cochées. Ce n’est pas le solde bancaire. " +
                "Chaque mois est une page neuve. Pour reporter : un revenu « Reste du mois passé », une fois."
        ),
        Section(
            title = "Courses",
            body = "Petites dépenses sans facture fixe. Lien « Courses » sous le Reste : montant puis " +
                "« J’ai dépensé » ou « Corriger ». Le Reste après factures ne change pas. " +
                "« Après courses » montre le reste réel (brique s’il est négatif)."
        ),
        Section(
            title = "Marquer FAIT",
            body = "Quand c’est payé, touchez « FAIT ✓ ». La ligne affiche « FAIT le … ». « Remettre » si erreur. " +
                "Montant différent ce mois : ouvrez la ligne — seul ce mois change, sauf si " +
                "« Aussi les mois suivants » est coché."
        ),
        Section(
            title = "En retard",
            body = "Date passée et pas encore FAIT : badge « En retard » (brique). " +
                "Rappel chaque jour à 10 h jusqu’au FAIT."
        ),
        Section(
            title = "Alertes",
            body = "Par facture : 3 jours avant à 10 h, le jour à 9 h, puis chaque matin à 10 h tant que ce n’est pas payé. " +
                "Si rien ne sonne : Réglages → Alertes. " +
                DeviceCareCopy.HINT
        ),
        Section(
            title = "Facture papier",
            body = "« Scanner une facture » lit le QR belge. « Copier » met la communication au presse-papiers. " +
                "« Voir la photo » ouvre le papier. « Joindre un PDF » range le courrier à côté. " +
                "« Lire +++ sur la photo » cherche seulement la communication (jamais le montant). " +
                "Sur une facture répétée, photo et communication suivent si « Aussi les mois suivants » est coché. " +
                "« Envoyer une copie » emporte aussi les photos dans l’archive."
        ),
        Section(
            title = "Retard ou accord",
            body = "Ouvrez « C’est un retard ou un accord ? » seulement pour un plan. " +
                "L’accueil montre le versement et une pastille « Accord », pas le total de la dette. " +
                "Chaque FAIT baisse le reste. Menu → « Mes dossiers » pour le reste dû et la barre. " +
                "IBAN, e-mail (Écrire), frais et courriers sont dans la fiche. " +
                "« Préparer un résumé » / « PDF du résumé » : pour un créancier ou un conseiller."
        ),
        Section(
            title = "Mettre de côté",
            body = "Réglages → « Mettre de côté » : virement mensuel vers un autre compte, dans le Reste dès le 1er. " +
                "FAIT quand c’est viré. Pas d’argent dans l’app — c’est sur votre autre compte. " +
                "Accueil : « Mis de côté : X € / mois »."
        ),
        Section(
            title = "Sur une année",
            body = "Mensuel = accueil. « 1× par an » ou « tous les 3 mois » : créées une fois, le bon mois. " +
                "Aperçu : flèche ›, Menu → « À venir », ou Réglages → « Échéances hors ce mois ». " +
                "Fréquence erronée : changez-la dans la ligne. Historique : calendrier du Menu."
        ),
        Section(
            title = "Sauvegarde",
            body = "À chaque changement : copie dans Documents (3 versions) + photos. " +
                "« Envoyer une copie » = archive (WhatsApp / Mail). " +
                "Nouveau téléphone : Réglages → Restaurer."
        ),
        Section(
            title = "Empreinte (optionnel)",
            body = "Par défaut l’app s’ouvre sans code. " +
                "Cochez « Verrouiller… » seulement pour cacher le mois aux autres."
        )
    )
}
