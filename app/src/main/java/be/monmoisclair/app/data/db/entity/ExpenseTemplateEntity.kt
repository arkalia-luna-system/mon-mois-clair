package be.monmoisclair.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_templates")
data class ExpenseTemplateEntity(
    @PrimaryKey val id: String,
    val label: String,
    val amountCents: Long,
    val dayOfMonth: Int,
    val active: Boolean,
    val remindJ3: Boolean = true,
    val remindJd: Boolean = true,
    val createdAtEpochMs: Long,
    val archivedAtEpochMs: Long?
)
