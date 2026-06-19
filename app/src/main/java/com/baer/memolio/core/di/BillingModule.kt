package com.baer.memolio.core.di

import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.billing.EntitlementRepositoryImpl
import com.baer.memolio.core.billing.RevenueCatClient
import com.baer.memolio.core.billing.RevenueCatClientImpl
import com.baer.memolio.feature.paywall.AndroidConnectivity
import com.baer.memolio.feature.paywall.Connectivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {
    @Binds
    @Singleton
    abstract fun bindEntitlementRepository(impl: EntitlementRepositoryImpl): EntitlementRepository

    @Binds
    @Singleton
    abstract fun bindRevenueCatClient(impl: RevenueCatClientImpl): RevenueCatClient

    @Binds
    abstract fun bindConnectivity(impl: AndroidConnectivity): Connectivity

    companion object {
        /** Paywall offerings loader, backed by the RevenueCat seam. */
        @Provides
        fun provideOfferingsLoader(
            client: RevenueCatClient
        ): @JvmSuppressWildcards suspend () -> List<String> =
            { client.loadOfferingTitles() }
    }
}
