package com.baer.memolio.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: String,
    val name: String,
    val coverPhotoId: String?,
    val createdAt: Long,
    val sortOrder: Int
)
