package com.baer.memolio.core.di

import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.billing.EntitlementRepositoryImpl
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
    // RevenueCatClient binding is added in the next task (needs RevenueCatClientImpl).
}
