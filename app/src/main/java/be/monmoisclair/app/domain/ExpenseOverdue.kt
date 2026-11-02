package be.monmoisclair.app.domain

import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.LocalDate
import java.time.YearMonth

/**
 * Règle F : EN RETARD si TODO et aujourd’hui (Brussels) > date d’échéance clampée.
 * « Mettre de côté » = geste volontaire → jamais pastille / notif / widget En retard
 * (doit rester aligné avec [be.monmoisclair.app.domain.usecase.ConfigureSavingsUseCase.LABEL]).
 */
object ExpenseOverdue {
    const val VOLUNTARY_SAVINGS_LABEL = "Mettre de côté"

    fun appliesToLabel(label: String): Boolean = label != VOLUNTARY_SAVINGS_LABEL

    fun isOverdue(
        ledgerId: String,
        dayOfMonth: Int,
        status: ExpenseStatus,
        today: LocalDate = AppClock.today(),
        label: String = ""
    ): Boolean {
        if (!appliesToLabel(label)) return false
        if (status != ExpenseStatus.TODO) return false
        val ym = runCatching { YearMonth.parse(ledgerId) }.getOrNull() ?: return false
        // Mois futur (aperçu) : jamais « En retard ».
        if (ym.isAfter(YearMonth.from(today))) return false
        val dueDay = MonthDayClamp.clamp(dayOfMonth, ym)
        val due = ym.atDay(dueDay)
        return today.isAfter(due)
    }
}
