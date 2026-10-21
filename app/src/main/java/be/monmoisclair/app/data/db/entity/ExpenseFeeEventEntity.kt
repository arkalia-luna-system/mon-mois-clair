package be.monmoisclair.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Historique daté des frais ajoutés par le créancier (lettres successives).
 * Le cumul [ExpenseTemplateEntity.addedFeesCents] reste la source pour le reste dû.
 */
@Entity(
    tableName = "expense_fee_events",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("templateId")]
)
data class ExpenseFeeEventEntity(
    @PrimaryKey val id: String,
    val templateId: String,
    val amountCents: Long,
    val note: String = "",
    val createdAtEpochMs: Long
)
