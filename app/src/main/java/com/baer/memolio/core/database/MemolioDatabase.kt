package com.baer.memolio.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PhotoEntity::class, AlbumEntity::class],
    version = 2,
    exportSchema = true
)
abstract class MemolioDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun albumDao(): AlbumDao
}

/** v2 adds `photos.inPlaylist` (per-photo slideshow include flag); existing rows default to 1. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE photos ADD COLUMN inPlaylist INTEGER NOT NULL DEFAULT 1")
    }
}
