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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AppSettingsRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun newRepo(scope: TestScope, fileName: String = "appsettings.preferences_pb"): SettingsRepository {
        val store: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { tmp.root.resolve(fileName) }
        )
        return SettingsRepositoryImpl(store)
    }

    @Test
    fun appSettingsDefaultsWhenEmpty() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this)
        repo.appSettings.test {
            val s = awaitItem()
            assertThat(s.uploadToken).isEmpty()
            assertThat(s.serverPort).isEqualTo(8080)
            assertThat(s.autostartEnabled).isTrue()
            assertThat(s.ambientDimming).isTrue()
            assertThat(s.brightness).isEqualTo(0.7f)
            assertThat(s.autoCleanup).isFalse()
            assertThat(s.wallpaperId).isEqualTo("default")
            assertThat(s.onboardingComplete).isFalse()
            assertThat(s.proUnlocked).isFalse()
            assertThat(s.sleepEnabled).isFalse()
            assertThat(s.sleepStartMinutes).isEqualTo(22 * 60)
            assertThat(s.sleepEndMinutes).isEqualTo(7 * 60)
        }
    }

    @Test
    fun setUploadTokenPersistsAndEmits() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this, "token.preferences_pb")
        repo.setUploadToken("mytoken123")
        repo.appSettings.test {
            assertThat(awaitItem().uploadToken).isEqualTo("mytoken123")
        }
    }

    @Test
    fun ensureTokenGeneratesOnFirstRunThenIsStable() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this, "ensure.preferences_pb")
        val first = repo.ensureToken()
        assertThat(first).hasLength(32)
        assertThat(first).matches("[0-9a-f]{32}")
        val second = repo.ensureToken()
        assertThat(second).isEqualTo(first)
        repo.appSettings.test {
            assertThat(awaitItem().uploadToken).isEqualTo(first)
        }
    }

    @Test
    fun rotateTokenReplacesTheToken() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this, "rotate.preferences_pb")
        val original = repo.ensureToken()
        val rotated = repo.rotateToken()
        assertThat(rotated).isNotEqualTo(original)
        assertThat(rotated).matches("[0-9a-f]{32}")
        repo.appSettings.test {
            assertThat(awaitItem().uploadToken).isEqualTo(rotated)
        }
    }

    @Test
    fun setServerPortPersists() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this, "port.preferences_pb")
        repo.setServerPort(9090)
        repo.appSettings.test {
            assertThat(awaitItem().serverPort).isEqualTo(9090)
        }
    }

    @Test
    fun setSleepRoundTrips() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this, "sleep.preferences_pb")
        repo.setSleep(enabled = true, startMinutes = 1320, endMinutes = 420)
        repo.appSettings.test {
            val s = awaitItem()
            assertThat(s.sleepEnabled).isTrue()
            assertThat(s.sleepStartMinutes).isEqualTo(1320)
            assertThat(s.sleepEndMinutes).isEqualTo(420)
        }
    }

    @Test
    fun setProUnlockedReflectsInAppSettings() = runTest(UnconfinedTestDispatcher()) {
        val repo = newRepo(this, "pro.preferences_pb")
        repo.setProUnlocked(true)
        repo.appSettings.test {
            assertThat(awaitItem().proUnlocked).isTrue()
        }
    }
}
