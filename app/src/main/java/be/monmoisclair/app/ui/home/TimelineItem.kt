package be.monmoisclair.app.ui.home

sealed interface TimelineItem {
    val sortDay: Int
    val sortLabel: String

    data class Income(
        val id: String,
        val dayLabel: String,
        val label: String,
        val amountLabel: String,
        override val sortDay: Int,
        override val sortLabel: String
    ) : TimelineItem

    data class Expense(
        val id: String,
        val dayLabel: String,
        val label: String,
        val amountLabel: String,
        val isDone: Boolean,
        /** Ex. « FAIT le 04/10/26 » — null si pas encore coché. */
        val doneLabel: String?,
        val isOverdue: Boolean,
        val isAutoDebit: Boolean,
        val paymentNote: String = "",
        val hasReceipt: Boolean = false,
        val receiptRelativePath: String = "",
        /** Lot B — « pour quoi » (vide si facture ordinaire). */
        val causeLabel: String = "",
        /** Lot B — pastille Accord (dossier avec reste de départ). */
        val isAgreement: Boolean = false,
        /** Intervalle du modèle (1 = mensuel). Affiché seulement si ≠ 1. */
        val intervalMonths: Int = 1,
        override val sortDay: Int,
        override val sortLabel: String
    ) : TimelineItem {
        val cadenceHint: String?
            get() = when (intervalMonths.coerceAtLeast(1)) {
                1 -> null
                3 -> "tous les 3 mois"
                6 -> "tous les 6 mois"
                12 -> "1× par an"
                else -> "tous les $intervalMonths mois"
            }
    }
}
