package be.monmoisclair.app.domain

/**
 * Les deux lignes du widget. Verrouillé : aucun montant, pour ne pas
 * afficher le mois sur l’écran d’accueil quand l’empreinte est cochée.
 */
data class CarnetWidgetText(
    val title: String,
    val amount: String,
    val amountNegative: Boolean,
    val line: String,
    val lineOverdue: Boolean,
    val locked: Boolean
) {
    companion object {
        fun compose(
            locked: Boolean,
            summary: MonthSummary,
            next: NextUnpaidLine.Line?,
            expenseCount: Int
        ): CarnetWidgetText {
            if (locked) {
                return CarnetWidgetText(
                    title = "Mon Mois Clair",
                    amount = "",
                    amountNegative = false,
                    line = "Touchez pour ouvrir",
                    lineOverdue = false,
                    locked = true
                )
            }
            val line = when {
                next != null && next.overdue ->
                    "En retard · ${next.label} · ${EuroFormat.formatCents(next.amountCents)}"
                next != null ->
                    "${next.label} · ${EuroFormat.formatCents(next.amountCents)}"
                expenseCount > 0 -> "Tout est payé"
                else -> "Rien à payer"
            }
            return CarnetWidgetText(
                title = "Reste",
                amount = EuroFormat.formatCents(summary.remainingCents),
                amountNegative = summary.remainingCents < 0,
                line = line,
                lineOverdue = next?.overdue == true,
                locked = false
            )
        }
    }
}
