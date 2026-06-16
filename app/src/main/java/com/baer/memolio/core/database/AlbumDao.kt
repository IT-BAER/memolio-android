package com.baer.memolio.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(album: AlbumEntity)

    @Query("SELECT * FROM albums ORDER BY sortOrder, createdAt")
    fun observeAlbums(): Flow<List<AlbumEntity>>

    @Query("DELETE FROM albums WHERE id = :id")
    suspend fun delete(id: String)
}
