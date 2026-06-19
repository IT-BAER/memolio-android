package com.baer.memolio.core.billing

import android.app.Activity
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The single Pro-entitlement entry point. isPro is backed by AppSettings.proUnlocked and is
 * STICKY-TRUE: once true it never reverts (a one-time purchase is permanent), so the
 * otherwise-offline frame never needs the network again. refresh/purchase/restore are the
 * ONLY networked operations; all SDK work goes through the RevenueCatClient seam.
 */
interface EntitlementRepository {
    val isPro: Flow<Boolean>
    suspend fun refresh()
    suspend fun purchase(activity: Activity): PurchaseResult
    suspend fun restore(): RestoreResult
}

class EntitlementRepositoryImpl @Inject constructor(
    private val settings: SettingsRepository,
    private val client: RevenueCatClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : EntitlementRepository {

    override val isPro: Flow<Boolean> =
        settings.appSettings
            .map { it.proUnlocked }
            .scan(false) { sticky, current -> sticky || current }
            .drop(1)
            .distinctUntilChanged()

    override suspend fun refresh() = withContext(ioDispatcher) {
        if (client.isEntitled()) settings.setProUnlocked(true)
    }

    override suspend fun purchase(activity: Activity): PurchaseResult =
        withContext(ioDispatcher) {
            val result = client.purchase(activity)
            if (result is PurchaseResult.Success) settings.setProUnlocked(true)
            result
        }

    override suspend fun restore(): RestoreResult = withContext(ioDispatcher) {
        val result = client.restore()
        if (result is RestoreResult.Success) settings.setProUnlocked(true)
        result
    }
}
