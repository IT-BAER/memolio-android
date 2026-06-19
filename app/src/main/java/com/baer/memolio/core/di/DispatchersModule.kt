package com.baer.memolio.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/**
 * Seed for the slideshow shuffle. A qualified [Long] (not a Kotlin default param,
 * which Hilt ignores) so production gets a fresh wall-clock seed while tests inject
 * a fixed value for a deterministic order.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ShuffleSeed

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @IoDispatcher
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @ShuffleSeed
    fun providesShuffleSeed(): Long = System.currentTimeMillis()
}
