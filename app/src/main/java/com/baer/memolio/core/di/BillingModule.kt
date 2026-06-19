package com.baer.memolio.core.di

import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.billing.EntitlementRepositoryImpl
import com.baer.memolio.core.billing.RevenueCatClient
import com.baer.memolio.core.billing.RevenueCatClientImpl
import dagger.Binds
import dagger.Module
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
}
