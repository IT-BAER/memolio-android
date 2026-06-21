package com.baer.memolio.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.media.FaceDetector
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

/**
 * One-time backfill that runs face detection over all live photos that have no focal point yet
 * (uploaded or seeded before the smart-crop feature). Idempotent: only selects photos where
 * focalX == null, so re-running skips already-processed ones. Missing display-cache files are
 * silently skipped; detector returning null (no face / failure) is also skipped. The worker
 * always returns [Result.success] so WorkManager does not retry or surface an error.
 *
 * Constructed by [FaceBackfillWorkerFactory] (manual factory, NOT @HiltWorker).
 */
class FaceBackfillWorker(
    appContext: Context,
    params: WorkerParameters,
    private val photoRepository: PhotoRepository,
    private val faceDetector: FaceDetector,
    private val ioDispatcher: CoroutineDispatcher
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        val photos = photoRepository.observeAllLivePhotos().first()
            .filter { it.focalX == null }

        photos.forEach { photo ->
            val displayFile = File(photo.displayCachePath)
            if (!displayFile.exists()) return@forEach
            val fp = faceDetector.detectFocalPoint(displayFile) ?: return@forEach
            photoRepository.setFocalPoint(photo.id, fp.x, fp.y)
        }

        Result.success()
    }
}

class FaceBackfillWorkerFactory(
    private val photoRepository: PhotoRepository,
    private val faceDetector: FaceDetector,
    private val ioDispatcher: CoroutineDispatcher
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? =
        if (workerClassName == FaceBackfillWorker::class.java.name) {
            FaceBackfillWorker(appContext, workerParameters, photoRepository, faceDetector, ioDispatcher)
        } else {
            null
        }
}

object FaceBackfillScheduler {

    const val UNIQUE_NAME = "face_backfill"

    fun schedule(context: Context) {
        val request = OneTimeWorkRequestBuilder<FaceBackfillWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}
