package com.baer.memolio.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Robolectric is required to initialise android.os.Build.VERSION.SDK_INT >= 26 so that
// DataStore's FileMoves_androidKt.atomicMoveTo uses Files.move(REPLACE_EXISTING) instead
// of File.renameTo, which silently fails on Windows when the target already exists.
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun newRepo(scope: TestScope, fileName: String = "settings.preferences_pb"): SettingsRepository {
        val store: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { tmp.root.resolve(fileName) }
        )
        return SettingsRepositoryImpl(store)
    }

    @Test
    fun defaultsAreReturnedWhenEmpty() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this)
        repo.playlistConfig.test {
            val cfg = awaitItem()
            assertThat(cfg.intervalSeconds).isEqualTo(30)
            assertThat(cfg.shuffle).isTrue()
            assertThat(cfg.showClock).isTrue()
        }
    }

    @Test
    fun updatesPersistAndEmit() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this, "settings2.preferences_pb")
        repo.setIntervalSeconds(60)
        repo.setShuffle(false)
        repo.playlistConfig.test {
            val cfg = awaitItem()
            assertThat(cfg.intervalSeconds).isEqualTo(60)
            assertThat(cfg.shuffle).isFalse()
        }
    }

    @Test
    fun clockStyleDefaultsToDigitalAndPersists() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this, "settings_clockstyle.preferences_pb")
        repo.playlistConfig.test {
            assertThat(awaitItem().clockStyle).isEqualTo(ClockStyle.DIGITAL)
            cancelAndIgnoreRemainingEvents()
        }
        repo.setClockStyle(ClockStyle.ANALOG)
        repo.playlistConfig.test {
            assertThat(awaitItem().clockStyle).isEqualTo(ClockStyle.ANALOG)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clockOpacityDefaultsToOneAndPersists() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this, "settings_clockopacity.preferences_pb")
        repo.playlistConfig.test {
            assertThat(awaitItem().clockOpacity).isEqualTo(1f)
            cancelAndIgnoreRemainingEvents()
        }
        repo.setClockOpacity(0.5f)
        repo.playlistConfig.test {
            assertThat(awaitItem().clockOpacity).isEqualTo(0.5f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clockScaleDefaultsToOneAndPersists() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this, "settings_clockscale.preferences_pb")
        repo.playlistConfig.test {
            assertThat(awaitItem().clockScale).isEqualTo(1f)
            cancelAndIgnoreRemainingEvents()
        }
        repo.setClockScale(1.25f)
        repo.playlistConfig.test {
            assertThat(awaitItem().clockScale).isEqualTo(1.25f)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
