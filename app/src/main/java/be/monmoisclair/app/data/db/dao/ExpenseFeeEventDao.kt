package be.monmoisclair.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import be.monmoisclair.app.data.db.entity.ExpenseFeeEventEntity

@Dao
interface ExpenseFeeEventDao {
    @Query(
        "SELECT * FROM expense_fee_events WHERE templateId = :templateId " +
            "ORDER BY createdAtEpochMs DESC, id DESC"
    )
    suspend fun listByTemplate(templateId: String): List<ExpenseFeeEventEntity>

    @Query("SELECT * FROM expense_fee_events ORDER BY createdAtEpochMs ASC, id ASC")
    suspend fun getAll(): List<ExpenseFeeEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: ExpenseFeeEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(events: List<ExpenseFeeEventEntity>)

    @Query("DELETE FROM expense_fee_events")
    suspend fun clearAll()
}
