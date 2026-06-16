package com.baer.memolio.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PhotoEntity::class, AlbumEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MemolioDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun albumDao(): AlbumDao
}
