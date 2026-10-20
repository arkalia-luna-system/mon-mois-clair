package be.monmoisclair.app.ui.settings

import be.monmoisclair.app.domain.usecase.OffMonthPeriodic
import be.monmoisclair.app.ui.format.MoneyFormat

/** Affichage des échéances hors mois (Réglages) — testable sans ViewModel. */
object OffMonthPeriodicFormat {
    fun amountLine(item: OffMonthPeriodic): String {
        val every = when (item.intervalMonths) {
            3 -> "tous les 3 mois"
            12 -> "1× par an"
            else -> "tous les ${item.intervalMonths} mois"
        }
        return "${MoneyFormat.formatCents(item.amountCents)} · le ${item.dayOfMonth} · $every"
    }
}
