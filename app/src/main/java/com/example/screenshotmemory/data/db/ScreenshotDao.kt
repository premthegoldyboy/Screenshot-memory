package com.example.screenshotmemory.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {

    @Query("SELECT * FROM screenshots ORDER BY dateTaken DESC")
    fun getAllScreenshotsFlow(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): ScreenshotEntity?

    @Query("SELECT uri FROM screenshots")
    suspend fun getAllUris(): List<String>

    @Query("SELECT * FROM screenshots WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScreenshotEntity?

    @Query("SELECT * FROM screenshots WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: Long): Flow<ScreenshotEntity?>

    @Query("SELECT * FROM screenshots WHERE indexingStatus = 'PENDING' ORDER BY dateTaken DESC")
    suspend fun getPendingOcrItems(): List<ScreenshotEntity>

    @Query("SELECT COUNT(*) FROM screenshots")
    fun getTotalCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM screenshots WHERE indexingStatus = 'COMPLETED'")
    fun getIndexedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM screenshots WHERE indexingStatus = 'PENDING'")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT * FROM screenshots WHERE isFavorite = 1 ORDER BY dateTaken DESC")
    fun getFavoriteScreenshotsFlow(): Flow<List<ScreenshotEntity>>

    @Query("SELECT COUNT(*) FROM screenshots WHERE isFavorite = 1")
    fun getFavoriteCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ScreenshotEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ScreenshotEntity): Long

    @Update
    suspend fun update(item: ScreenshotEntity)

    @Query("DELETE FROM screenshots WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM screenshots WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)

    @Query("DELETE FROM screenshots")
    suspend fun deleteAll()

    @Query("DELETE FROM screenshots WHERE uri NOT IN (:validUris)")
    suspend fun deleteMissingUris(validUris: List<String>)
}
