package be.monmoisclair.app.ui.settings

/**
 * Aide « Comment ça marche » — textes figés, testables, ton familial.
 * Affiché depuis Réglages (contact = Menu Accueil uniquement).
 */
object HowItWorksCopy {
    const val TITLE = "Comment ça marche"

    data class Section(val title: String, val body: String)

    val sections: List<Section> = listOf(
        Section(
            title = "Le chiffre en haut",
            body = "« Reste après factures » = revenus − factures du mois — pas le solde bancaire. " +
                "« À payer » = encore dû. « Après les FAIT » (brun) = seulement les cochées. " +
                "Pas de report auto : un revenu « Reste du mois passé », une fois."
        ),
        Section(
            title = "Courses",
            body = "Ligne sous le Reste (›) : courses / pharmacie. " +
                "Le gros Reste ne bouge pas ; « Après courses » montre le solde."
        ),
        Section(
            title = "Marquer FAIT",
            body = "Payé : « FAIT ✓ », puis « FAIT le … ». « Remettre » si erreur. " +
                "Prélèvement : cochez quand vous voyez le débit, pas le jour prévu. " +
                "Autre montant ce mois : ouvrez la ligne — seul ce mois change, " +
                "sauf « Aussi les mois suivants »."
        ),
        Section(
            title = "En retard",
            body = "Date passée et pas FAIT : badge brique. Rappel chaque jour à 10 h jusqu’au FAIT."
        ),
        Section(
            title = "Alertes",
            body = "3 j avant à 10 h, le jour à 9 h, puis chaque matin à 10 h si impayé. " +
                "Rien ne sonne : Réglages → Alertes. " +
                DeviceCareCopy.HINT
        ),
        Section(
            title = "Facture papier",
            body = "« Scanner une facture » lit le QR belge. « Copier » pour la banque. " +
                "Photo et PDF à côté. « Lire la communication sur la photo » = " +
                "communication seulement — jamais le montant. Eau / énergie : souvent " +
                "une nouvelle +++ chaque mois (loyer ou prélèvement : la ref peut rester)."
        ),
        Section(
            title = "Retard ou accord",
            body = "Plan (à qui, pour quoi, reste dû) : pastille « Accord » à l’accueil, " +
                "pas le total de la dette. Chaque FAIT baisse le reste. " +
                "Menu → Dossiers. Résumé texte/PDF pour créancier."
        ),
        Section(
            title = "Mettre de côté",
            body = "Ligne sous le Reste (sticky, comme Courses ; sinon derrière « Détails »). " +
                "Ou Réglages pour le jour. FAIT = virement fait. Pas d’argent dans l’app."
        ),
        Section(
            title = "Sur une année",
            body = "« 1× par an » / « tous les 3 mois » : réapparaissent le bon mois. " +
                "Aperçu : calendrier Accueil ou Menu → À venir. " +
                "Hors ce mois : Réglages → Échéances hors ce mois."
        ),
        Section(
            title = "Sauvegarde",
            body = "Documents garde 3 copies protégées. Notez le code (Réglages → Sauvegarde) " +
                "une fois sur papier. « Envoyer une copie » : jamais le code et le .mmc " +
                "dans le même message. Nouveau téléphone : Restaurer + ce code. " +
                "Anciennes ZIP claires : à jeter quand une bonne copie est notée."
        ),
        Section(
            title = "Empreinte",
            body = "Optionnel : cochez pour cacher le mois aux autres. " +
                "Les alertes masquent alors les montants."
        )
    )
}
