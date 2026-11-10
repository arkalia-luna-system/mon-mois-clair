package be.monmoisclair.app.ui.home

import java.time.YearMonth

/**
 * B10 — bascule de mois calendaire au resume :
 * n’écrase pas une navigation faite pendant [MonthCycleEngine.ensureCurrentMonthExists].
 */
object HomeMonthRoll {

    fun monthAfterRollIfUnchanged(
        selectedBeforeEnsure: YearMonth,
        selectedNow: YearMonth,
        calendar: YearMonth
    ): YearMonth = if (selectedNow == selectedBeforeEnsure) calendar else selectedNow
}
