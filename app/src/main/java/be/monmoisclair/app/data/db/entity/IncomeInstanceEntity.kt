package be.monmoisclair.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "income_instances",
    foreignKeys = [
        ForeignKey(
            entity = MonthLedgerEntity::class,
            parentColumns = ["id"],
            childColumns = ["ledgerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ledgerId"), Index("templateId")]
)
data class IncomeInstanceEntity(
    @PrimaryKey val id: String,
    val ledgerId: String,
    val templateId: String?,
    val label: String,
    val amountCents: Long,
    val dayOfMonth: Int,
    val createdAtEpochMs: Long
)
