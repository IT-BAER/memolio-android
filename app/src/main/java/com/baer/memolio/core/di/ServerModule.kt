package com.baer.memolio.core.di

import android.content.Context
import com.baer.memolio.core.data.AlbumRepository
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.media.BitmapTranscoder
import com.baer.memolio.core.media.MediaImporter
import com.baer.memolio.core.media.Transcoder
import com.baer.memolio.core.server.AssetLoader
import com.baer.memolio.core.server.FrameServer
import com.baer.memolio.core.server.FrameServerDeps
import com.baer.memolio.core.server.SettingsTokenProvider
import com.baer.memolio.core.server.TokenProvider
import com.baer.memolio.core.server.UploadUrlProvider
import com.baer.memolio.core.server.UploadUrlProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServerBindingsModule {
    /** [MediaImporter] depends on the [Transcoder] interface; bind the real implementation. */
    @Binds
    abstract fun bindTranscoder(impl: BitmapTranscoder): Transcoder

    @Binds
    @Singleton
    abstract fun bindUploadUrlProvider(impl: UploadUrlProviderImpl): UploadUrlProvider
}

@Module
@InstallIn(SingletonComponent::class)
object ServerModule {

    /**
     * Production [TokenProvider] per shared-contract addendum C: a [SettingsTokenProvider]
     * holding a `@Volatile` token kept in sync by collecting `settings.appSettings` on an
     * app-scoped [CoroutineScope] (NOT a one-shot `runBlocking` read), so `rotateToken()`
     * invalidates old upload links on the very next request without a server restart.
     */
    @Provides
    @Singleton
    fun provideTokenProvider(
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        settings: SettingsRepository
    ): TokenProvider {
        val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
        return SettingsTokenProvider.start(scope, settings)
    }

    @Provides
    @Singleton
    fun provideAssetLoader(@ApplicationContext context: Context): AssetLoader =
        AssetLoader { path -> context.assets.open(path) }

    @Provides
    @Singleton
    fun provideFrameServer(
        tokenProvider: TokenProvider,
        importer: MediaImporter,
        albums: AlbumRepository,
        assetLoader: AssetLoader
    ): FrameServer = FrameServer(FrameServerDeps(tokenProvider, importer, albums, assetLoader))
}
