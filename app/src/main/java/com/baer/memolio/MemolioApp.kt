package com.baer.memolio

import android.app.Application
import androidx.work.Configuration
import com.baer.memolio.appliance.TimeProvider
import com.baer.memolio.core.data.AlbumRepository
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.di.IoDispatcher
import com.baer.memolio.core.media.FaceDetector
import com.baer.memolio.core.model.Album
import com.baer.memolio.core.storage.FileStorage
import com.baer.memolio.work.FaceBackfillScheduler
import com.baer.memolio.work.FaceBackfillWorkerFactory
import com.baer.memolio.work.TrashPurgeScheduler
import com.baer.memolio.work.TrashPurgeWorkerFactory
import kotlinx.coroutines.CoroutineDispatcher
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MemolioApp : Application(), Configuration.Provider {

    @Inject lateinit var photoRepository: PhotoRepository
    @Inject lateinit var fileStorage: FileStorage
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var timeProvider: TimeProvider
    @Inject lateinit var albumRepository: AlbumRepository
    @Inject lateinit var faceDetector: FaceDetector
    @Inject @IoDispatcher lateinit var ioDispatcher: CoroutineDispatcher

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() {
            val delegating = androidx.work.DelegatingWorkerFactory()
            delegating.addFactory(
                TrashPurgeWorkerFactory(
                    photoRepository = photoRepository,
                    fileStorage = fileStorage,
                    settings = settingsRepository.appSettings,
                    time = timeProvider
                )
            )
            delegating.addFactory(
                FaceBackfillWorkerFactory(
                    photoRepository = photoRepository,
                    faceDetector = faceDetector,
                    ioDispatcher = ioDispatcher
                )
            )
            return Configuration.Builder()
                .setWorkerFactory(delegating)
                .build()
        }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            if (albumRepository.observeAlbums().first().none { it.id == "all" }) {
                albumRepository.upsert(
                    Album(
                        id = "all", name = "All photos", coverPhotoId = null,
                        createdAt = System.currentTimeMillis(), sortOrder = 0
                    )
                )
            }
        }
        TrashPurgeScheduler.schedule(this)
        FaceBackfillScheduler.schedule(this)

        // RevenueCat configure does NOT hit the network here (the SDK lazily fetches on the
        // first awaitOfferings/awaitCustomerInfo). Guarded so a blank/bad key or odd device
        // state never crashes the otherwise-offline frame. When the key is blank (dev/test),
        // configure is skipped and billing calls surface as PurchaseResult/RestoreResult.Error.
        runCatching {
            if (BuildConfig.REVENUECAT_API_KEY.isNotBlank()) {
                Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR
                Purchases.configure(
                    PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_API_KEY).build()
                )
            }
        }
    }
}
