package be.monmoisclair.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import be.monmoisclair.app.data.db.entity.AppMetaEntity

@Dao
interface AppMetaDao {

    @Query("SELECT * FROM app_meta WHERE `key` = :key")
    suspend fun get(key: String): AppMetaEntity?

    @Query("SELECT * FROM app_meta")
    suspend fun getAll(): List<AppMetaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meta: AppMetaEntity)
}
