package com.baer.memolio

import android.app.Application
import androidx.work.Configuration
import com.baer.memolio.appliance.TimeProvider
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.storage.FileStorage
import com.baer.memolio.work.TrashPurgeScheduler
import com.baer.memolio.work.TrashPurgeWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MemolioApp : Application(), Configuration.Provider {

    @Inject lateinit var photoRepository: PhotoRepository
    @Inject lateinit var fileStorage: FileStorage
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var timeProvider: TimeProvider

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(
                TrashPurgeWorkerFactory(
                    photoRepository = photoRepository,
                    fileStorage = fileStorage,
                    settings = settingsRepository.appSettings,
                    time = timeProvider
                )
            )
            .build()

    override fun onCreate() {
        super.onCreate()
        TrashPurgeScheduler.schedule(this)
    }
}
