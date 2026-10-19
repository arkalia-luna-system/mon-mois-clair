package be.monmoisclair.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income_templates")
data class IncomeTemplateEntity(
    @PrimaryKey val id: String,
    val label: String,
    val amountCents: Long,
    val dayOfMonth: Int,
    val active: Boolean,
    val createdAtEpochMs: Long,
    val archivedAtEpochMs: Long?
)
