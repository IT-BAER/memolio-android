package com.baer.memolio.core.data

import com.baer.memolio.core.database.PhotoDao
import com.baer.memolio.core.database.PhotoEntity
import com.baer.memolio.core.database.toDomain
import com.baer.memolio.core.di.IoDispatcher
import com.baer.memolio.core.model.Photo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface PhotoRepository {
    fun observePhotos(albumId: String): Flow<List<Photo>>
    fun observeTrash(): Flow<List<Photo>>
    /**
     * Live photos across the union of [albumIds], trashed photos excluded.
     * An empty set short-circuits to an empty list: we never run `... IN ()`,
     * which SQLite treats as always-false but is wasteful and easy to misread.
     */
    fun observePhotosInAlbums(albumIds: Set<String>): Flow<List<Photo>>
    /**
     * Every live photo (the whole pool), trashed excluded. Backs the frame's
     * "no album filter selected" case: an empty active-album set means "show
     * everything", which is the default free-tier slideshow source.
     */
    fun observeAllLivePhotos(): Flow<List<Photo>>
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
    suspend fun moveToAlbum(id: String, albumId: String)
    suspend fun setFavorite(id: String, favorite: Boolean)
    suspend fun setCaption(id: String, caption: String?)
    suspend fun reorder(orderedIds: List<String>)
}

class PhotoRepositoryImpl @Inject constructor(
    private val photoDao: PhotoDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PhotoRepository {

    override fun observePhotos(albumId: String): Flow<List<Photo>> =
        photoDao.observeLivePhotos(albumId).map { list -> list.map { it.toDomain() } }

    override fun observeTrash(): Flow<List<Photo>> =
        photoDao.observeTrash().map { list -> list.map { it.toDomain() } }

    override fun observePhotosInAlbums(albumIds: Set<String>): Flow<List<Photo>> {
        if (albumIds.isEmpty()) return flowOf(emptyList())
        return photoDao.observeLivePhotosInAlbums(albumIds)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeAllLivePhotos(): Flow<List<Photo>> =
        photoDao.observeAllLivePhotos().map { list -> list.map { it.toDomain() } }

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

    override suspend fun moveToAlbum(id: String, albumId: String) =
        withContext(ioDispatcher) { photoDao.updateAlbum(id, albumId) }

    override suspend fun setFavorite(id: String, favorite: Boolean) =
        withContext(ioDispatcher) { photoDao.updateFavorite(id, favorite) }

    override suspend fun setCaption(id: String, caption: String?) =
        withContext(ioDispatcher) { photoDao.updateCaption(id, caption) }

    /** Persists [orderedIds] as ascending sortOrder (index 0 = first). */
    override suspend fun reorder(orderedIds: List<String>) = withContext(ioDispatcher) {
        orderedIds.forEachIndexed { index, id -> photoDao.updateSortOrder(id, index) }
    }
}
