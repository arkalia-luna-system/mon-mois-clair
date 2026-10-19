package be.monmoisclair.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import be.monmoisclair.app.domain.model.ExpenseStatus

@Entity(
    tableName = "expense_instances",
    foreignKeys = [
        ForeignKey(
            entity = MonthLedgerEntity::class,
            parentColumns = ["id"],
            childColumns = ["ledgerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ledgerId"), Index("templateId"), Index("status")]
)
data class ExpenseInstanceEntity(
    @PrimaryKey val id: String,
    val ledgerId: String,
    val templateId: String?,
    val label: String,
    val amountCents: Long,
    val dayOfMonth: Int,
    val status: ExpenseStatus,
    val doneAtEpochMs: Long?,
    val remindJ3: Boolean,
    val remindJd: Boolean,
    val isAutoDebit: Boolean = false,
    val paymentNote: String = "",
    val receiptRelativePath: String = "",
    val alarmJ3RequestCode: Int?,
    val alarmJdRequestCode: Int?,
    val createdAtEpochMs: Long
)
