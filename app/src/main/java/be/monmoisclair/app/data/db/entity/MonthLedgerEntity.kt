package be.monmoisclair.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "month_ledgers")
data class MonthLedgerEntity(
    @PrimaryKey val id: String,
    val yearMonth: String,
    /** Somme « courses / divers » saisie rapidement (centimes). */
    val diversSpentCents: Long = 0L,
    val createdAtEpochMs: Long
)
