package com.baer.memolio.core.di

import com.baer.memolio.core.time.SystemTimeProvider
import com.baer.memolio.core.time.TimeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeModule {

    @Binds
    abstract fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider
}
