package be.monmoisclair.app.domain

import be.monmoisclair.app.domain.model.ExpenseStatus
import java.time.LocalDate
import java.time.YearMonth

/** Règle F : EN RETARD si TODO et aujourd’hui (Brussels) > date d’échéance clampée. */
object ExpenseOverdue {
    fun isOverdue(
        ledgerId: String,
        dayOfMonth: Int,
        status: ExpenseStatus,
        today: LocalDate = AppClock.today()
    ): Boolean {
        if (status != ExpenseStatus.TODO) return false
        val ym = runCatching { YearMonth.parse(ledgerId) }.getOrNull() ?: return false
        // Mois futur (aperçu) : jamais « En retard ».
        if (ym.isAfter(YearMonth.from(today))) return false
        val dueDay = MonthDayClamp.clamp(dayOfMonth, ym)
        val due = ym.atDay(dueDay)
        return today.isAfter(due)
    }
}
