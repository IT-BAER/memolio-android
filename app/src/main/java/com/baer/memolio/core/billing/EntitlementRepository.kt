package com.baer.memolio.core.billing

import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.di.IoDispatcher
import com.baer.memolio.core.time.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * The single Pro-entitlement entry point. isPro follows the cached AppSettings.proUnlocked,
 * so the frame keeps its last known state offline. refresh() mirrors RevenueCat's answer,
 * which re-locks Pro after a refund. refresh() is the only networked operation here (the
 * RevenueCatUI paywall runs purchase and restore); SDK calls go through the RevenueCatClient seam.
 */
interface EntitlementRepository {
    val isPro: Flow<Boolean>
    suspend fun refresh()
    /** Checks Pro at app start, but only while Pro is active or a purchase is pending. */
    suspend fun refreshOnAppStart()
    /** Remembers a pending Pro purchase so the next app starts can pick it up. */
    suspend fun markPurchasePending()
}

class EntitlementRepositoryImpl @Inject constructor(
    private val settings: SettingsRepository,
    private val client: RevenueCatClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val time: TimeProvider,
    // Hilt passes BuildConfig.DEBUG (see BillingModule); tests default to false to exercise
    // the real sticky logic. When true, every Pro surface unlocks for on-device testing.
    private val debugUnlock: Boolean = false
) : EntitlementRepository {

    override val isPro: Flow<Boolean> =
        if (debugUnlock) {
            flowOf(true)
        } else {
            settings.appSettings
                .map { it.proUnlocked }
                .distinctUntilChanged()
        }

    override suspend fun refresh() = withContext(ioDispatcher) {
        val entitled = try {
            client.isEntitled()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Offline or billing unavailable: keep the last known state.
            return@withContext
        }
        settings.setProUnlocked(entitled)
        if (entitled) settings.setPurchasePendingSince(0L)
    }

    override suspend fun refreshOnAppStart() {
        val s = settings.appSettings.first()
        val pendingDays = if (s.purchasePendingSince > 0L) today() - s.purchasePendingSince else null
        val pending = pendingDays != null && pendingDays <= PENDING_DAYS
        if (pendingDays != null && !pending) settings.setPurchasePendingSince(0L)
        if (s.proUnlocked || pending) refresh()
    }

    override suspend fun markPurchasePending() = settings.setPurchasePendingSince(today())

    private fun today(): Long = time.now().toLocalDate().toEpochDay()

    companion object {
        /** A purchase still pending after this many days counts as abandoned. */
        const val PENDING_DAYS = 14L
    }
}
