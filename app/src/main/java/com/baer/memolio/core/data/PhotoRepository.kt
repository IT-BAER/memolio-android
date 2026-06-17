package com.baer.memolio.core.data

import com.baer.memolio.core.database.PhotoDao
import com.baer.memolio.core.database.PhotoEntity
import com.baer.memolio.core.database.toDomain
import com.baer.memolio.core.model.Photo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface PhotoRepository {
    fun observePhotos(albumId: String): Flow<List<Photo>>
    fun observeTrash(): Flow<List<Photo>>
    suspend fun isDuplicate(contentHash: String): Boolean
    suspend fun add(
        id: String,
        originalPath: String,
        displayCachePath: String,
        thumbPath: String,
        contentHash: String,
        width: Int,
        height: Int,
        orientation: Int,
        caption: String?,
        albumId: String,
        sourceDevice: String?,
        addedAt: Long
    )
    suspend fun softDelete(id: String, now: Long)
    suspend fun restore(id: String)
    suspend fun purgeTrashOlderThan(threshold: Long): Int
}

class PhotoRepositoryImpl @Inject constructor(
    private val photoDao: PhotoDao,
    private val ioDispatcher: CoroutineDispatcher
) : PhotoRepository {

    override fun observePhotos(albumId: String): Flow<List<Photo>> =
        photoDao.observeLivePhotos(albumId).map { list -> list.map { it.toDomain() } }

    override fun observeTrash(): Flow<List<Photo>> =
        photoDao.observeTrash().map { list -> list.map { it.toDomain() } }

    override suspend fun isDuplicate(contentHash: String): Boolean =
        withContext(ioDispatcher) { photoDao.existsByHash(contentHash) }

    @Suppress("LongParameterList")
    override suspend fun add(
        id: String,
        originalPath: String,
        displayCachePath: String,
        thumbPath: String,
        contentHash: String,
        width: Int,
        height: Int,
        orientation: Int,
        caption: String?,
        albumId: String,
        sourceDevice: String?,
        addedAt: Long
    ) = withContext(ioDispatcher) {
        photoDao.upsert(
            PhotoEntity(
                id = id,
                originalPath = originalPath,
                displayCachePath = displayCachePath,
                thumbPath = thumbPath,
                contentHash = contentHash,
                width = width,
                height = height,
                orientation = orientation,
                caption = caption,
                albumId = albumId,
                favorite = false,
                sortOrder = 0,
                addedAt = addedAt,
                sourceDevice = sourceDevice,
                deletedAt = null
            )
        )
    }

    override suspend fun softDelete(id: String, now: Long) =
        withContext(ioDispatcher) { photoDao.softDelete(id, now) }

    override suspend fun restore(id: String) =
        withContext(ioDispatcher) { photoDao.restore(id) }

    override suspend fun purgeTrashOlderThan(threshold: Long): Int =
        withContext(ioDispatcher) { photoDao.purgeTrashOlderThan(threshold) }
}
