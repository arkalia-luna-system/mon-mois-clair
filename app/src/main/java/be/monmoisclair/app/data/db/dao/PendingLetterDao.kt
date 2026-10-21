package be.monmoisclair.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import be.monmoisclair.app.data.db.entity.PendingLetterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingLetterDao {
    @Query("SELECT * FROM pending_letters ORDER BY createdAtEpochMs DESC")
    suspend fun getAll(): List<PendingLetterEntity>

    @Query("SELECT * FROM pending_letters ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<PendingLetterEntity>>

    @Query("SELECT * FROM pending_letters WHERE id = :id")
    suspend fun getById(id: String): PendingLetterEntity?

    @Query("SELECT COUNT(*) FROM pending_letters")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM pending_letters")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(letter: PendingLetterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(letters: List<PendingLetterEntity>)

    @Query("DELETE FROM pending_letters WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pending_letters")
    suspend fun clearAll()
}
