package be.monmoisclair.app.domain

/**
 * Politique de propagation de la communication (`+++` / note de paiement).
 *
 * Loyer / huissier / prélèvement : la ref est souvent stable → on peut recopier.
 * Eau, énergie, télécom… : chaque facture a un nouvel OGM → recopier = faux positif banque.
 *
 * Charte : ne pas inventer de montant ; ici on évite seulement de coller une ref morte.
 */
object PaymentNotePolicy {

    /**
     * Vrai pour dossiers (reste dû) et prélèvements automatiques.
     * Faux pour une facture ordinaire récurrente.
     */
    fun shouldCarryForward(
        openingRemainingCents: Long?,
        isAutoDebit: Boolean
    ): Boolean = DossierRemaining.isDossier(openingRemainingCents) || isAutoDebit

    /**
     * Note à coller sur une **nouvelle** instance (cycle / futures).
     * Facture ordinaire → vide (saisie / QR / OCR du mois) ; dossier / Auto → note du modèle.
     */
    fun noteForNewInstance(
        templatePaymentNote: String,
        openingRemainingCents: Long?,
        isAutoDebit: Boolean
    ): String =
        if (shouldCarryForward(openingRemainingCents, isAutoDebit)) {
            templatePaymentNote
        } else {
            ""
        }
}
