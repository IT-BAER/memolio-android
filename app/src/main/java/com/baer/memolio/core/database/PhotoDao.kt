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

    @Query(
        "SELECT * FROM photos WHERE albumId IN (:albumIds) AND deletedAt IS NULL " +
            "ORDER BY sortOrder, addedAt"
    )
    fun observeLivePhotosInAlbums(albumIds: Set<String>): Flow<List<PhotoEntity>>

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

    @Query("UPDATE photos SET albumId = :albumId WHERE id = :id")
    suspend fun updateAlbum(id: String, albumId: String)

    @Query("UPDATE photos SET favorite = :favorite WHERE id = :id")
    suspend fun updateFavorite(id: String, favorite: Boolean)

    @Query("UPDATE photos SET caption = :caption WHERE id = :id")
    suspend fun updateCaption(id: String, caption: String?)

    @Query("UPDATE photos SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)
}
