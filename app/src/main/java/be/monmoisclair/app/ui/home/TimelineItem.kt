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
        /** Ex. « FAIT le 04/10 » — null si pas encore coché. */
        val doneLabel: String?,
        val isOverdue: Boolean,
        val isAutoDebit: Boolean,
        val paymentNote: String = "",
        val hasReceipt: Boolean = false,
        val receiptRelativePath: String = "",
        override val sortDay: Int,
        override val sortLabel: String
    ) : TimelineItem
}
