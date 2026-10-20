package be.monmoisclair.app.ui.settings

/**
 * Aide « Comment ça marche » — textes figés, testables, ton familial.
 * Affiché depuis Réglages (Home freeze : pas de nouveau bouton sur l’accueil).
 */
object HowItWorksCopy {
    const val TITLE = "Comment ça marche"

    data class Section(val title: String, val body: String)

    val sections: List<Section> = listOf(
        Section(
            title = "Le chiffre en haut",
            body = "Le gros chiffre, c’est le « Reste après factures » : vos revenus du mois moins toutes les factures. " +
                "La ligne en dessous dit ce qu’il reste à payer. " +
                "Ce n’est pas le solde de votre compte en banque. " +
                "Chaque mois est une page neuve : l’app ne reporte pas toute seule ce qui restait. " +
                "Pour le noter, ajoutez un revenu « Reste du mois passé », une seule fois."
        ),
        Section(
            title = "Marquer FAIT",
            body = "Quand vous avez payé (ou vérifié le prélèvement), touchez « FAIT ✓ » à droite. " +
                "La ligne devient verte et affiche « FAIT le … » (date exacte) — preuve rassurante. " +
                "« Remettre » si vous vous êtes trompée. " +
                "Si le montant change ce mois-ci, ouvrez la ligne et corrigez le chiffre : " +
                "seul ce mois change, tant que « Aussi les mois suivants » n’est pas coché."
        ),
        Section(
            title = "EN RETARD",
            body = "Si la date est passée et que ce n’est pas encore FAIT, un badge « EN RETARD » apparaît " +
                "(brique, pas un rouge qui fait peur). " +
                "L’app vous rappelle chaque jour à 10 h jusqu’à ce que ce soit FAIT."
        ),
        Section(
            title = "Courses / divers",
            body = "Pour les petites dépenses sans facture fixe (courses, pharmacie…). " +
                "Tapez un montant puis « + Divers » ou « − Divers »."
        ),
        Section(
            title = "Rappels",
            body = "Pour chaque facture : 3 jours avant (10 h), le jour J (9 h), " +
                "puis chaque jour à 10 h si c’est en retard. " +
                "Vérifiez « Santé des rappels » et Samsung Device Care si rien ne sonne."
        ),
        Section(
            title = "Facture papier",
            body = "À l’ajout : « Scanner une facture » lit le QR belge (EPC) et préremplit nom, montant " +
                "et communication. Touchez « Copier la communication » puis collez dans votre app bancaire. " +
                "Vous pouvez aussi prendre une photo de la facture. Elle se revoit en ouvrant la ligne. " +
                "Une copie reste dans Documents. « Envoyer une copie » l’emporte aussi dans l’archive."
        ),
        Section(
            title = "Sur une année",
            body = "Les factures mensuelles restent sur l’accueil. " +
                "Assurances, taxes ou factures 1×/an : créez-les une fois (fréquence « 1× par an » ou « tous les 3 mois »). " +
                "Elles réapparaissent le bon mois ; les autres mois, regardez Réglages → « Échéances hors ce mois ». " +
                "Pour feuilleter l’historique : Menu (barre du bas) → calendrier des mois, " +
                "ou les flèches à côté du mois. " +
                "Le jour d’échéance se choisit en touchant un grand numéro, de 1 à 31. " +
                "Si Menu affiche « Alertes », les rappels ne peuvent pas sonner : " +
                "Menu → Vérifier les alertes (sinon : Santé des rappels)."
        ),
        Section(
            title = "Sauvegarde",
            body = "À chaque changement, une copie des mois est écrite dans Documents (3 versions), " +
                "avec les photos de factures dans le même dossier. " +
                "« Envoyer une copie » (WhatsApp / Mail) envoie une archive : mois et photos. " +
                "En cas de nouveau téléphone : Réglages → Restaurer (fichier ou archive)."
        ),
        Section(
            title = "Empreinte (optionnel)",
            body = "Par défaut l’app s’ouvre sans code. " +
                "Cochez « Verrouiller… » seulement si vous voulez cacher le mois aux autres."
        )
    )
}
