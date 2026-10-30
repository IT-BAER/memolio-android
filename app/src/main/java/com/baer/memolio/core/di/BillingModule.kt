package com.baer.memolio.core.di

import com.baer.memolio.BuildConfig
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.billing.EntitlementRepositoryImpl
import com.baer.memolio.core.billing.RevenueCatClient
import com.baer.memolio.core.billing.RevenueCatClientImpl
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.time.TimeProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {
    @Binds
    @Singleton
    abstract fun bindRevenueCatClient(impl: RevenueCatClientImpl): RevenueCatClient

    companion object {
        @Provides
        @Singleton
        fun provideEntitlementRepository(
            settings: SettingsRepository,
            client: RevenueCatClient,
            @IoDispatcher ioDispatcher: CoroutineDispatcher,
            time: TimeProvider
        ): EntitlementRepository =
            // Debug builds unlock all Pro surfaces for testing; release keeps the real gate.
            EntitlementRepositoryImpl(settings, client, ioDispatcher, time, debugUnlock = BuildConfig.DEBUG)
    }
}
