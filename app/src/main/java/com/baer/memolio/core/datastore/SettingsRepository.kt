package com.baer.memolio.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
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

    // AppSettings
    val appSettings: Flow<AppSettings>
    suspend fun setUploadToken(token: String)
    suspend fun setServerPort(port: Int)
    suspend fun setSleep(enabled: Boolean, startMinutes: Int, endMinutes: Int)
    suspend fun setKioskEnabled(value: Boolean)
    suspend fun setHomeAppEnabled(value: Boolean)
    suspend fun setAutostartEnabled(value: Boolean)
    suspend fun setAmbientDimming(value: Boolean)
    suspend fun setBrightness(value: Float)
    suspend fun setAutoCleanup(value: Boolean)
    suspend fun setWallpaperId(value: String)
    suspend fun setOnboardingComplete(value: Boolean)
    suspend fun setProUnlocked(value: Boolean)
    suspend fun rotateToken(): String
    suspend fun ensureToken(): String
}

class SettingsRepositoryImpl @Inject constructor(
    private val store: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        // PlaylistConfig keys
        val ACTIVE_ALBUMS = stringSetPreferencesKey("active_albums")
        val SHUFFLE = booleanPreferencesKey("shuffle")
        val INTERVAL = intPreferencesKey("interval_seconds")
        val TRANSITION = stringPreferencesKey("transition")
        val FIT_MODE = stringPreferencesKey("fit_mode")
        val SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val SHOW_CAPTION = booleanPreferencesKey("show_caption")

        // AppSettings keys
        val UPLOAD_TOKEN = stringPreferencesKey("upload_token")
        val SERVER_PORT = intPreferencesKey("server_port")
        val SLEEP_ENABLED = booleanPreferencesKey("sleep_enabled")
        val SLEEP_START = intPreferencesKey("sleep_start_minutes")
        val SLEEP_END = intPreferencesKey("sleep_end_minutes")
        val KIOSK_ENABLED = booleanPreferencesKey("kiosk_enabled")
        val HOME_APP_ENABLED = booleanPreferencesKey("home_app_enabled")
        val AUTOSTART_ENABLED = booleanPreferencesKey("autostart_enabled")
        val AMBIENT_DIMMING = booleanPreferencesKey("ambient_dimming")
        val BRIGHTNESS = floatPreferencesKey("brightness")
        val AUTO_CLEANUP = booleanPreferencesKey("auto_cleanup")
        val WALLPAPER_ID = stringPreferencesKey("wallpaper_id")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val PRO_UNLOCKED = booleanPreferencesKey("pro_unlocked")
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

    override val appSettings: Flow<AppSettings> = store.data.map { p ->
        val d = AppSettings()
        AppSettings(
            uploadToken = p[Keys.UPLOAD_TOKEN] ?: d.uploadToken,
            serverPort = p[Keys.SERVER_PORT] ?: d.serverPort,
            sleepEnabled = p[Keys.SLEEP_ENABLED] ?: d.sleepEnabled,
            sleepStartMinutes = p[Keys.SLEEP_START] ?: d.sleepStartMinutes,
            sleepEndMinutes = p[Keys.SLEEP_END] ?: d.sleepEndMinutes,
            kioskEnabled = p[Keys.KIOSK_ENABLED] ?: d.kioskEnabled,
            homeAppEnabled = p[Keys.HOME_APP_ENABLED] ?: d.homeAppEnabled,
            autostartEnabled = p[Keys.AUTOSTART_ENABLED] ?: d.autostartEnabled,
            ambientDimming = p[Keys.AMBIENT_DIMMING] ?: d.ambientDimming,
            brightness = p[Keys.BRIGHTNESS] ?: d.brightness,
            autoCleanup = p[Keys.AUTO_CLEANUP] ?: d.autoCleanup,
            wallpaperId = p[Keys.WALLPAPER_ID] ?: d.wallpaperId,
            onboardingComplete = p[Keys.ONBOARDING_COMPLETE] ?: d.onboardingComplete,
            proUnlocked = p[Keys.PRO_UNLOCKED] ?: d.proUnlocked,
        )
    }

    override suspend fun setUploadToken(token: String) { store.edit { it[Keys.UPLOAD_TOKEN] = token } }
    override suspend fun setServerPort(port: Int) { store.edit { it[Keys.SERVER_PORT] = port } }
    override suspend fun setSleep(enabled: Boolean, startMinutes: Int, endMinutes: Int) {
        store.edit {
            it[Keys.SLEEP_ENABLED] = enabled
            it[Keys.SLEEP_START] = startMinutes
            it[Keys.SLEEP_END] = endMinutes
        }
    }
    override suspend fun setKioskEnabled(value: Boolean) { store.edit { it[Keys.KIOSK_ENABLED] = value } }
    override suspend fun setHomeAppEnabled(value: Boolean) { store.edit { it[Keys.HOME_APP_ENABLED] = value } }
    override suspend fun setAutostartEnabled(value: Boolean) { store.edit { it[Keys.AUTOSTART_ENABLED] = value } }
    override suspend fun setAmbientDimming(value: Boolean) { store.edit { it[Keys.AMBIENT_DIMMING] = value } }
    override suspend fun setBrightness(value: Float) { store.edit { it[Keys.BRIGHTNESS] = value } }
    override suspend fun setAutoCleanup(value: Boolean) { store.edit { it[Keys.AUTO_CLEANUP] = value } }
    override suspend fun setWallpaperId(value: String) { store.edit { it[Keys.WALLPAPER_ID] = value } }
    override suspend fun setOnboardingComplete(value: Boolean) { store.edit { it[Keys.ONBOARDING_COMPLETE] = value } }
    override suspend fun setProUnlocked(value: Boolean) { store.edit { it[Keys.PRO_UNLOCKED] = value } }

    /** Returns the existing token, generating + persisting one on first run. */
    override suspend fun ensureToken(): String {
        val existing = store.data.first()[Keys.UPLOAD_TOKEN]
        if (!existing.isNullOrEmpty()) return existing
        val generated = newToken()
        store.edit { it[Keys.UPLOAD_TOKEN] = generated }
        return generated
    }

    /** Invalidates the old QR by replacing the token. Returns the new token. */
    override suspend fun rotateToken(): String {
        val generated = newToken()
        store.edit { it[Keys.UPLOAD_TOKEN] = generated }
        return generated
    }

    private fun newToken(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
