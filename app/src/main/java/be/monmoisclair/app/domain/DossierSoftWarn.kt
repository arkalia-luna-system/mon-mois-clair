package be.monmoisclair.app.domain

/**
 * Signaux doux sur une fiche accord — jamais bloquants à l’enregistrement.
 * Info manquante → signaler, pas supprimer ni inventer.
 */
object DossierSoftWarn {

    const val MISSING_CAUSE =
        "Pour quoi vide — utile pour retrouver l’accord plus tard."
    const val MISSING_PHOTO =
        "Pas de photo du courrier — vous pourrez l’ajouter."
    const val MISSING_CONTACT =
        "IBAN et e-mail absents — utiles pour Copier / Écrire."

    /**
     * @param isAgreementOrOpening true si accord déjà ouvert **ou** reste dû en cours de saisie
     * @param hasPhoto photo ou PDF déjà attaché (pending ou stocké)
     */
    fun messages(
        isAgreementOrOpening: Boolean,
        causeLabel: String,
        hasPhoto: Boolean,
        iban: String,
        creditorEmail: String
    ): List<String> {
        if (!isAgreementOrOpening) return emptyList()
        return buildList {
            if (causeLabel.isBlank()) add(MISSING_CAUSE)
            if (!hasPhoto) add(MISSING_PHOTO)
            if (iban.isBlank() && creditorEmail.isBlank()) add(MISSING_CONTACT)
        }
    }
}
