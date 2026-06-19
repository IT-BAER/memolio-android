package com.baer.memolio.core.di

import com.baer.memolio.appliance.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplianceModule {
    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider = TimeProvider.System
}
