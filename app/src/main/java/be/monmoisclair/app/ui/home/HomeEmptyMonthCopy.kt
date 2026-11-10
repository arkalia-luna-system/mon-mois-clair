package be.monmoisclair.app.ui.home

/** Copy empty Accueil — testable hors Compose. */
object HomeEmptyMonthCopy {

    fun title(isPastMonth: Boolean, isFuturePreview: Boolean): String = when {
        isFuturePreview -> "Rien de prévu ce mois-là"
        isPastMonth -> "Aucune ligne ce mois-là"
        else -> "Rien encore ce mois"
    }

    fun body(isPastMonth: Boolean, isFuturePreview: Boolean): String = when {
        isFuturePreview ->
            "Les lignes réapparaissent ici quand elles sont dues. " +
                "Pour en ajouter, revenez au mois en cours."
        isPastMonth ->
            "Rien n’a été noté sur ce mois. Revenez au mois en cours pour ajouter."
        else ->
            "Ajoutez un revenu ou une dépense avec « + Ajouter » en bas."
    }

    /** CTA barre = « Mois en cours » (pas + Ajouter) sur passé / futur. */
    fun showGoCurrentCta(isPastMonth: Boolean, isFuturePreview: Boolean): Boolean =
        isPastMonth || isFuturePreview
}
