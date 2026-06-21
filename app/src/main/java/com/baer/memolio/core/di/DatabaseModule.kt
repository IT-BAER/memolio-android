package com.baer.memolio.core.di

import android.content.Context
import androidx.room.Room
import com.baer.memolio.core.database.AlbumDao
import com.baer.memolio.core.database.MIGRATION_1_2
import com.baer.memolio.core.database.MIGRATION_2_3
import com.baer.memolio.core.database.MemolioDatabase
import com.baer.memolio.core.database.PhotoDao
import com.baer.memolio.core.storage.FileStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MemolioDatabase =
        Room.databaseBuilder(context, MemolioDatabase::class.java, "memolio.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun providePhotoDao(db: MemolioDatabase): PhotoDao = db.photoDao()

    @Provides
    fun provideAlbumDao(db: MemolioDatabase): AlbumDao = db.albumDao()

    @Provides
    @Singleton
    fun provideFileStorage(@ApplicationContext context: Context): FileStorage =
        FileStorage(context.filesDir)
}
