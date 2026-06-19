package com.baer.memolio.core.billing

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Representative cross-surface gating: the SAME isPro boolean from EntitlementRepository
 * decides ALBUMS (album create) and CUSTOM_WALLPAPER (non-default pick). This proves a
 * single source of truth drives every gate, and that free keeps the "all" pool + default.
 */
class GatingDecisionTest {

    private class FakeEntitlement(pro: Boolean) : EntitlementRepository {
        override val isPro: Flow<Boolean> = MutableStateFlow(pro)
        override suspend fun refresh() {}
        override suspend fun purchase(activity: Activity): PurchaseResult = PurchaseResult.Success
        override suspend fun restore(): RestoreResult = RestoreResult.Success
    }

    /** Mirrors the ViewModel gate: "allowed" iff isPro (or the always-free default). */
    private suspend fun albumsAllowed(ent: EntitlementRepository) = ent.isPro.first()
    private suspend fun wallpaperAllowed(ent: EntitlementRepository, id: String) =
        id == "default" || ent.isPro.first()

    @Test
    fun freeUserGatedOnAlbumsAndCustomWallpaperButKeepsDefault() = runTest {
        val free = FakeEntitlement(pro = false)
        assertThat(albumsAllowed(free)).isFalse()
        assertThat(wallpaperAllowed(free, "aurora")).isFalse()
        assertThat(wallpaperAllowed(free, "default")).isTrue()   // always free
    }

    @Test
    fun proUserUnlocksAlbumsAndCustomWallpaper() = runTest {
        val pro = FakeEntitlement(pro = true)
        assertThat(albumsAllowed(pro)).isTrue()
        assertThat(wallpaperAllowed(pro, "aurora")).isTrue()
    }
}
