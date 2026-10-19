package be.monmoisclair.app.ui.entry

/** Libellés français de fréquence (formulaire / édition / aide). */
object RecurrenceLabel {
    /** Phrase longue (case édition / récap). */
    fun forIntervalMonths(intervalMonths: Int): String = when (intervalMonths.coerceAtLeast(1)) {
        1 -> "Revient tous les mois"
        3 -> "Revient tous les 3 mois"
        12 -> "Revient 1× par an"
        else -> "Revient tous les $intervalMonths mois"
    }

    fun oneOff(): String = "Une seule fois (ce mois-ci)"

    /** Chips compacts (saisie). */
    fun chipOnce(): String = "Une seule fois"
    fun chipMonthly(): String = "Tous les mois"
    fun chipEvery3Months(): String = "Tous les 3 mois"
    fun chipYearly(): String = "1× par an"
}
