package com.baer.memolio.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE albumId = :albumId AND deletedAt IS NULL ORDER BY sortOrder, addedAt")
    fun observeLivePhotos(albumId: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE deletedAt IS NULL ORDER BY addedAt DESC")
    fun observeAllLivePhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeTrash(): Flow<List<PhotoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM photos WHERE contentHash = :hash)")
    suspend fun existsByHash(hash: String): Boolean

    @Query("UPDATE photos SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)

    @Query("UPDATE photos SET deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: String)

    @Query("DELETE FROM photos WHERE deletedAt IS NOT NULL AND deletedAt < :threshold")
    suspend fun purgeTrashOlderThan(threshold: Long): Int
}
