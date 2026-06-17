package com.baer.memolio.core.di

import com.baer.memolio.core.data.AlbumRepository
import com.baer.memolio.core.data.AlbumRepositoryImpl
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.data.PhotoRepositoryImpl
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.datastore.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository

    @Binds
    abstract fun bindAlbumRepository(impl: AlbumRepositoryImpl): AlbumRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
