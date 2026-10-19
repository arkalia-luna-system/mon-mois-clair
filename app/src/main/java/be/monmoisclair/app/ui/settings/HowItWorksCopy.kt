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
            body = "« Reste après factures » = vos revenus du mois moins toutes les factures. " +
                "Ce n’est pas le solde de votre compte en banque."
        ),
        Section(
            title = "Marquer FAIT",
            body = "Quand vous avez payé (ou vérifié le prélèvement), touchez « Marquer FAIT ✓ ». " +
                "La ligne reste visible en vert — elle n’est pas effacée. " +
                "Vous pouvez « Remettre à payer » si vous vous êtes trompée."
        ),
        Section(
            title = "EN RETARD",
            body = "Si la date est passée et que ce n’est pas encore FAIT, la ligne devient rouge. " +
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
            title = "Sauvegarde",
            body = "À chaque changement, une copie JSON est écrite dans Documents. " +
                "En cas de nouveau téléphone : Réglages → Restaurer. " +
                "Pour un téléphone cassé ou volé : Envoyer une copie (WhatsApp / Mail) à quelqu’un de confiance."
        ),
        Section(
            title = "Empreinte (optionnel)",
            body = "Par défaut l’app s’ouvre sans code. " +
                "Cochez « Verrouiller… » seulement si vous voulez cacher le mois aux autres."
        )
    )
}
