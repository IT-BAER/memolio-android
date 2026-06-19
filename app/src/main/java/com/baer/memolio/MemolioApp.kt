package com.baer.memolio

import android.app.Application
import androidx.work.Configuration
import com.baer.memolio.appliance.TimeProvider
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.storage.FileStorage
import com.baer.memolio.work.TrashPurgeScheduler
import com.baer.memolio.work.TrashPurgeWorkerFactory
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
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
