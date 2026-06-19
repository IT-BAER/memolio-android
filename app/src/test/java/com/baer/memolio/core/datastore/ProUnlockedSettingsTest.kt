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
class ProUnlockedSettingsTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun newRepo(scope: TestScope, fileName: String = "pro_settings.preferences_pb"): SettingsRepositoryImpl {
        val store: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { tmp.root.resolve(fileName) }
        )
        return SettingsRepositoryImpl(store)
    }

    @Test
    fun proUnlockedDefaultsToFalse() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this)
        repo.appSettings.test {
            assertThat(awaitItem().proUnlocked).isFalse()
        }
    }

    @Test
    fun setProUnlockedPersistsAndEmits() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this, "pro_settings2.preferences_pb")
        repo.setProUnlocked(true)
        repo.appSettings.test {
            assertThat(awaitItem().proUnlocked).isTrue()
        }
    }
}
