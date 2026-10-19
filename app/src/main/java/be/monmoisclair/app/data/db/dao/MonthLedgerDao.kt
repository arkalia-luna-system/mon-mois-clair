package be.monmoisclair.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import be.monmoisclair.app.data.db.entity.MonthLedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthLedgerDao {

    @Query("SELECT * FROM month_ledgers WHERE id = :id")
    suspend fun getById(id: String): MonthLedgerEntity?

    @Query("SELECT * FROM month_ledgers ORDER BY id DESC")
    fun observeAll(): Flow<List<MonthLedgerEntity>>

    @Query("SELECT * FROM month_ledgers ORDER BY id ASC")
    suspend fun getAll(): List<MonthLedgerEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(ledger: MonthLedgerEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(ledgers: List<MonthLedgerEntity>)

    @Query("DELETE FROM month_ledgers")
    suspend fun clearAll()
}
