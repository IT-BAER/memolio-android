package com.baer.memolio.core.data

import com.baer.memolio.core.database.AlbumDao
import com.baer.memolio.core.database.toDomain
import com.baer.memolio.core.database.toEntity
import com.baer.memolio.core.di.IoDispatcher
import com.baer.memolio.core.model.Album
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AlbumRepository {
    fun observeAlbums(): Flow<List<Album>>
    suspend fun upsert(album: Album)
    suspend fun delete(id: String)
}

class AlbumRepositoryImpl @Inject constructor(
    private val albumDao: AlbumDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AlbumRepository {

    override fun observeAlbums(): Flow<List<Album>> =
        albumDao.observeAlbums().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(album: Album) =
        withContext(ioDispatcher) { albumDao.upsert(album.toEntity()) }

    override suspend fun delete(id: String) =
        withContext(ioDispatcher) { albumDao.delete(id) }
}
