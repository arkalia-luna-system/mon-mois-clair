package be.monmoisclair.app.ui.settings

/**
 * Aide « Comment ça marche » — textes figés, testables, ton familial.
 * Affiché depuis Réglages (Menu = Réglages + mois seulement).
 */
object HowItWorksCopy {
    const val TITLE = "Comment ça marche"

    data class Section(val title: String, val body: String)

    val sections: List<Section> = listOf(
        Section(
            title = "Le chiffre en haut",
            body = "Le gros chiffre, c’est le « Reste après factures » : vos revenus du mois moins toutes les factures " +
                "(payées ou non). " +
                "« À payer » dit ce qui reste à régler. " +
                "« Hors à payer » (en brun) montre ce qu’il resterait si on ne comptait que les factures déjà FAIT. " +
                "Ce n’est pas le solde de votre compte en banque. " +
                "Chaque mois est une page neuve : l’app ne reporte pas toute seule ce qui restait. " +
                "Pour le noter, ajoutez un revenu « Reste du mois passé », une seule fois."
        ),
        Section(
            title = "Courses",
            body = "Pour les petites dépenses sans facture fixe (courses, pharmacie…). " +
                "Tapez un montant puis « Ajouter » ou « Retirer ». " +
                "Le gros chiffre « Reste après factures » ne change pas. " +
                "Une ligne « Après courses » montre ce qu’il resterait, à titre indicatif."
        ),
        Section(
            title = "Marquer FAIT",
            body = "Quand vous avez payé (ou vérifié le prélèvement), touchez « FAIT ✓ » à droite. " +
                "La ligne devient verte et affiche « FAIT le … » (jour, mois et année) — preuve rassurante. " +
                "« Remettre » si vous vous êtes trompée. " +
                "Si le montant change ce mois-ci, ouvrez la ligne et corrigez le chiffre : " +
                "seul ce mois change, tant que « Aussi les mois suivants » n’est pas coché."
        ),
        Section(
            title = "En retard",
            body = "Si la date est passée et que ce n’est pas encore FAIT, un badge « En retard » apparaît " +
                "(brique, pas un rouge qui fait peur). " +
                "L’app vous rappelle chaque jour à 10 h jusqu’à ce que ce soit FAIT."
        ),
        Section(
            title = "Alertes",
            body = "Pour chaque facture : 3 jours avant à 10 h, le jour même à 9 h, " +
                "puis chaque matin à 10 h tant que ce n’est pas payé. " +
                "Vérifiez Réglages → Alertes si rien ne sonne. " +
                "Dans Samsung, retirez Mon Mois Clair de la mise en veille."
        ),
        Section(
            title = "Facture papier",
            body = "À l’ajout : « Scanner une facture » lit le QR de la facture belge et préremplit nom, montant " +
                "et référence. Sur le mois, touchez « Copier ». Dans la fiche, « Copier la communication ». " +
                "Si c’est une référence bancaire valide (12 chiffres), le presse-papiers les reçoit " +
                "(l’écran garde le +++). Collez ensuite dans votre app bancaire. " +
                "Sur une facture répétée, la communication suit les mois suivants. " +
                "À l’ajout, la photo d’une facture qui revient est reprise les mois suivants. " +
                "Dans la fiche, elle ne suit que si vous cochez « Aussi les mois suivants ». " +
                "Sur le mois, « Voir la photo » (ou « Photo » sur petit écran) ouvre le papier. " +
                "Dans la fiche, touchez la vignette pour l’agrandir. " +
                "Vous pouvez reprendre la photo sans tout recréer. " +
                "Une copie reste dans Documents. « Envoyer une copie » l’emporte aussi dans l’archive."
        ),
        Section(
            title = "Retard ou accord",
            body = "Sous la fréquence, « C’est un retard ou un accord ? » reste fermé. " +
                "Ouvrez-le seulement pour un plan : à qui, pour quoi, et combien il reste aujourd’hui. " +
                "Le mois montre le versement et une pastille « Accord », pas le total de la dette. " +
                "Chaque FAIT baisse le reste du montant réellement payé. " +
                "Quand c’est soldé, la ligne ne revient plus le mois suivant. " +
                "La fiche dit « Soldé ». Les mois déjà payés restent. " +
                "Sur un accord : IBAN (Copier), e-mail (Écrire — ouvre l’app Mail, sans stocker les échanges), " +
                "frais (« Ils ont ajouté un montant »). " +
                "Une nouvelle photo s’ajoute aux courriers sans remplacer la première ; " +
                "vous pouvez voir ou enlever chaque courrier. " +
                "Le motif des frais affiché est le dernier saisi. " +
                "S’il y a au moins un accord : Menu → « Mes dossiers » " +
                "(reste dû, barre, et « encore environ N mois si le versement reste de X » " +
                "sur la fiche et Mes dossiers — seulement si chaque versement déjà FAIT " +
                "est resté au forfait ; rien de tout ça sur l’accueil). " +
                "Sans versement encore : « Ranger sans plan » (photo + qui + pour quoi) — " +
                "le courrier n’entre pas dans le Reste. Une ligne sur l’accueil " +
                "« courrier sans plan · Voir » s’affiche s’il y en a. " +
                "« Préparer un résumé » (Mes dossiers, ou Réglages si déficit) " +
                "écrit un texte à envoyer à un créancier ou à un conseiller — pas à tout le monde."
        ),
        Section(
            title = "Mettre de côté",
            body = "Réglages → « Mettre de côté » crée une facture mensuelle, virée vers un autre compte. " +
                "Elle entre dans le Reste dès le 1er, comme le loyer. " +
                "FAIT quand le virement est fait. " +
                "Si ce mois est déjà FAIT, le montant des mois suivants se change dans Réglages, " +
                "pas sur la fiche de ce virement. " +
                "Ce n’est pas de l’argent gardé dans l’application. " +
                "La fiche dit « Déjà viré » : cette somme n’est pas disponible ici. " +
                "Tant que vous ne l’activez pas, l’accueil ne change pas."
        ),
        Section(
            title = "Sur une année",
            body = "Les factures mensuelles restent sur l’accueil. " +
                "Assurances, taxes ou factures 1×/an : créez-les une fois (fréquence « 1× par an » ou « tous les 3 mois »). " +
                "Elles réapparaissent le bon mois ; les autres mois, regardez Réglages → « Échéances hors ce mois ». " +
                "Si vous vous êtes trompée de fréquence, ouvrez la ligne et changez-la : pas besoin de supprimer. " +
                "Pour feuilleter l’historique : Menu (barre du bas) → calendrier des mois, " +
                "ou les flèches à côté du mois. " +
                "Le jour d’échéance se choisit en touchant un grand numéro, de 1 à 31. " +
                "Aide, alertes et restauration : tout est dans Réglages."
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
