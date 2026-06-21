package com.baer.memolio.core.database

import com.baer.memolio.core.model.Album
import com.baer.memolio.core.model.Photo

fun PhotoEntity.toDomain() = Photo(
    id, originalPath, displayCachePath, thumbPath, contentHash, width, height,
    orientation, caption, albumId, favorite, sortOrder, addedAt, sourceDevice, deletedAt, inPlaylist,
    focalX, focalY
)

fun Photo.toEntity() = PhotoEntity(
    id, originalPath, displayCachePath, thumbPath, contentHash, width, height,
    orientation, caption, albumId, favorite, sortOrder, addedAt, sourceDevice, deletedAt, inPlaylist,
    focalX, focalY
)

fun AlbumEntity.toDomain() = Album(id, name, coverPhotoId, createdAt, sortOrder)

fun Album.toEntity() = AlbumEntity(id, name, coverPhotoId, createdAt, sortOrder)
