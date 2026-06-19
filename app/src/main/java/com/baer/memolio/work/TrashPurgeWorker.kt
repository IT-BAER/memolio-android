package com.baer.memolio.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.baer.memolio.appliance.PurgeThreshold
import com.baer.memolio.appliance.TimeProvider
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.storage.FileStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Periodic maintenance: when AppSettings.autoCleanup is on, delete trash rows whose
 * deletedAt is older than the 30-day cutoff (PurgeThreshold) AND remove their files via
 * FileStorage. When autoCleanup is off, no-op. Default autoCleanup is off (spec section 7).
 * Constructed by [TrashPurgeWorkerFactory] (installed via WorkManager Configuration) so
 * Room/FileStorage/Settings are injected and the worker is testable.
 */
class TrashPurgeWorker(
    appContext: Context,
    params: WorkerParameters,
    private val photoRepository: PhotoRepository,
    private val fileStorage: FileStorage,
    private val settings: Flow<AppSettings>,
    @Suppress("UNUSED_PARAMETER") time: TimeProvider,
    private val nowMillis: () -> Long
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val appSettings = settings.first()
        if (!appSettings.autoCleanup) return Result.success()

        val cutoff = PurgeThreshold.cutoff(nowMillis())

        val expired = photoRepository.observeTrash().first()
            .filter { p -> p.deletedAt?.let { it < cutoff } == true }

        photoRepository.purgeTrashOlderThan(cutoff)

        expired.forEach { photo ->
            val ext = photo.originalPath.substringAfterLast('.', "jpg")
            fileStorage.deleteAll(photo.id, ext)
        }
        return Result.success()
    }
}

class TrashPurgeWorkerFactory(
    private val photoRepository: PhotoRepository,
    private val fileStorage: FileStorage,
    private val settings: Flow<AppSettings>,
    private val time: TimeProvider,
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? =
        if (workerClassName == TrashPurgeWorker::class.java.name) {
            TrashPurgeWorker(appContext, workerParameters, photoRepository, fileStorage, settings, time, nowMillis)
        } else {
            null
        }
}
