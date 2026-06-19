package com.baer.memolio.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Enqueues the unique periodic trash-purge job. Runs ~daily under battery-not-low.
 * The worker itself early-returns when autoCleanup is off, so this can be scheduled
 * unconditionally at app start.
 */
object TrashPurgeScheduler {

    const val UNIQUE_NAME = "trash_purge"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<TrashPurgeWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
