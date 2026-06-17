package com.baer.memolio.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface SettingsRepository {
    val playlistConfig: Flow<PlaylistConfig>
    suspend fun setActiveAlbumIds(ids: Set<String>)
    suspend fun setShuffle(value: Boolean)
    suspend fun setIntervalSeconds(value: Int)
    suspend fun setTransition(value: TransitionStyle)
    suspend fun setFitMode(value: FitMode)
    suspend fun setShowClock(value: Boolean)
    suspend fun setShowDate(value: Boolean)
    suspend fun setShowCaption(value: Boolean)
}

class SettingsRepositoryImpl @Inject constructor(
    private val store: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val ACTIVE_ALBUMS = stringSetPreferencesKey("active_albums")
        val SHUFFLE = booleanPreferencesKey("shuffle")
        val INTERVAL = intPreferencesKey("interval_seconds")
        val TRANSITION = stringPreferencesKey("transition")
        val FIT_MODE = stringPreferencesKey("fit_mode")
        val SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val SHOW_CAPTION = booleanPreferencesKey("show_caption")
    }

    override val playlistConfig: Flow<PlaylistConfig> = store.data.map { p ->
        val defaults = PlaylistConfig()
        PlaylistConfig(
            activeAlbumIds = p[Keys.ACTIVE_ALBUMS] ?: defaults.activeAlbumIds,
            shuffle = p[Keys.SHUFFLE] ?: defaults.shuffle,
            intervalSeconds = p[Keys.INTERVAL] ?: defaults.intervalSeconds,
            transition = p[Keys.TRANSITION]?.let { TransitionStyle.valueOf(it) } ?: defaults.transition,
            fitMode = p[Keys.FIT_MODE]?.let { FitMode.valueOf(it) } ?: defaults.fitMode,
            showClock = p[Keys.SHOW_CLOCK] ?: defaults.showClock,
            showDate = p[Keys.SHOW_DATE] ?: defaults.showDate,
            showCaption = p[Keys.SHOW_CAPTION] ?: defaults.showCaption
        )
    }

    override suspend fun setActiveAlbumIds(ids: Set<String>) { store.edit { it[Keys.ACTIVE_ALBUMS] = ids } }
    override suspend fun setShuffle(value: Boolean) { store.edit { it[Keys.SHUFFLE] = value } }
    override suspend fun setIntervalSeconds(value: Int) { store.edit { it[Keys.INTERVAL] = value } }
    override suspend fun setTransition(value: TransitionStyle) { store.edit { it[Keys.TRANSITION] = value.name } }
    override suspend fun setFitMode(value: FitMode) { store.edit { it[Keys.FIT_MODE] = value.name } }
    override suspend fun setShowClock(value: Boolean) { store.edit { it[Keys.SHOW_CLOCK] = value } }
    override suspend fun setShowDate(value: Boolean) { store.edit { it[Keys.SHOW_DATE] = value } }
    override suspend fun setShowCaption(value: Boolean) { store.edit { it[Keys.SHOW_CAPTION] = value } }
}
