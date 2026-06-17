package com.baer.memolio.core.media

import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.di.IoDispatcher
import com.baer.memolio.core.storage.FileStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val DISPLAY_MAX_EDGE = 2560
private const val THUMB_MAX_EDGE = 480

/**
 * Turns uploaded bytes into a stored photo: hash -> dedup -> write original -> generate
 * display cache + thumbnail -> insert the Room row. Decode is delegated to [Transcoder]
 * (fakeable). The photo id is the first 32 hex chars of the content hash, so it is
 * deterministic and dedup-aligned (no random ids).
 */
class MediaImporter @Inject constructor(
    private val transcoder: Transcoder,
    private val fileStorage: FileStorage,
    private val photoRepository: PhotoRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    sealed interface ImportResult {
        data class Added(val id: String) : ImportResult
        data object Duplicate : ImportResult
        data class Rejected(val reason: String) : ImportResult
    }

    suspend fun import(
        bytes: ByteArray,
        ext: String,
        albumId: String,
        caption: String?,
        sourceDevice: String?,
        now: Long
    ): ImportResult = withContext(ioDispatcher) {
        val hash = Hasher.sha256(bytes)
        if (photoRepository.isDuplicate(hash)) return@withContext ImportResult.Duplicate

        val id = hash.take(32)
        val safeExt = ext.lowercase().trim('.', ' ')
        val original = fileStorage.writeOriginal(id, safeExt) { it.write(bytes) }

        // Probe the file; reject and clean up if not a decodable image.
        if (transcoder.readBounds(original) == null) {
            fileStorage.deleteAll(id, safeExt)
            return@withContext ImportResult.Rejected("unsupported or corrupt image")
        }

        val displayFile = fileStorage.displayCacheFile(id)
        val displayDims = transcoder.writeDownscaledJpeg(original, displayFile, DISPLAY_MAX_EDGE)
        transcoder.writeDownscaledJpeg(original, fileStorage.thumbFile(id), THUMB_MAX_EDGE)

        photoRepository.add(
            id = id,
            originalPath = original.path,
            displayCachePath = displayFile.path,
            thumbPath = fileStorage.thumbFile(id).path,
            contentHash = hash,
            width = displayDims.width,
            height = displayDims.height,
            orientation = 0,
            caption = caption,
            albumId = albumId,
            sourceDevice = sourceDevice,
            addedAt = now
        )
        ImportResult.Added(id)
    }
}
