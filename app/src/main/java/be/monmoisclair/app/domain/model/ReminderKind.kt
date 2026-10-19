package be.monmoisclair.app.domain.model

enum class ReminderKind {
    J3,
    JD,
    /** Relance quotidienne 10h tant que TODO et échéance dépassée. */
    OVERDUE
}
