package be.monmoisclair.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import be.monmoisclair.app.data.db.entity.AppMetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppMetaDao {

    @Query("SELECT * FROM app_meta WHERE `key` = :key")
    suspend fun get(key: String): AppMetaEntity?

    @Query("SELECT value FROM app_meta WHERE `key` = :key")
    fun observeValue(key: String): Flow<String?>

    @Query("SELECT * FROM app_meta")
    suspend fun getAll(): List<AppMetaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meta: AppMetaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AppMetaEntity>)

    @Query("DELETE FROM app_meta WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM app_meta")
    suspend fun clearAll()
}
