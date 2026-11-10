package be.monmoisclair.app.ui.home

/**
 * Style bandeau « Pour aujourd’hui » — logique testable hors Compose.
 */
object HomeTodayFocusCardStyle {
    /** Retards : filet brique épais (pas teinte verte). */
    fun overdueBorderWidthDp(hasOverdue: Boolean): Float =
        if (hasOverdue) 1.5f else 0.5f

    fun usesOverdueBorder(hasOverdue: Boolean): Boolean = hasOverdue

    fun actionLabel(hasOverdue: Boolean): String =
        if (hasOverdue) "Voir les retards" else "Voir l’échéance"
}
