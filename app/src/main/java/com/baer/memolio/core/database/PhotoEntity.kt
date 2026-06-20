package com.baer.memolio.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    foreignKeys = [ForeignKey(
        entity = AlbumEntity::class,
        parentColumns = ["id"],
        childColumns = ["albumId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("albumId"), Index("contentHash", unique = true), Index("deletedAt")]
)
data class PhotoEntity(
    @PrimaryKey val id: String,
    val originalPath: String,
    val displayCachePath: String,
    val thumbPath: String,
    val contentHash: String,
    val width: Int,
    val height: Int,
    val orientation: Int,
    val caption: String?,
    val albumId: String,
    val favorite: Boolean,
    val sortOrder: Int,
    val addedAt: Long,
    val sourceDevice: String?,
    val deletedAt: Long?,
    val inPlaylist: Boolean = true
)
