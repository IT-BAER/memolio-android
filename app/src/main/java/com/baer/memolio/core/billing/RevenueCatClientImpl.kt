package com.baer.memolio.core.billing

import android.content.Context
import com.baer.memolio.BuildConfig
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.awaitCustomerInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Real RevenueCat wiring (purchases-android 8.x). This is the ONLY class that imports
 * com.revenuecat.purchases, so an SDK drift is contained here. Device/network only:
 * requires Google Play Billing + a configured RevenueCat project. The callers of isEntitled()
 * are verified via EntitlementRepositoryTest against the fake RevenueCatClient.
 */
class RevenueCatClientImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RevenueCatClient {

    private val purchases: Purchases get() = Purchases.sharedInstance

    override suspend fun isEntitled(): Boolean {
        check(ensureConfigured(context)) { "RevenueCat is not configured in this build" }
        val info = purchases.awaitCustomerInfo(CacheFetchPolicy.FETCH_CURRENT)
        return info.entitlements[PRO_ENTITLEMENT_ID]?.isActive == true
    }

    companion object {
        /** The RevenueCat entitlement identifier configured in the dashboard. */
        const val PRO_ENTITLEMENT_ID = "pro"

        /**
         * Configures RevenueCat on first use. A configured SDK goes online at every app start and
         * foreground, so free frames that never open the Pro offer must stay unconfigured.
         * Returns false when the build has no RevenueCat key (debug builds).
         */
        @Synchronized
        fun ensureConfigured(context: Context): Boolean {
            if (Purchases.isConfigured) return true
            if (BuildConfig.REVENUECAT_API_KEY.isBlank()) return false
            runCatching {
                Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR
                Purchases.configure(
                    PurchasesConfiguration.Builder(context.applicationContext, BuildConfig.REVENUECAT_API_KEY).build()
                )
            }
            return Purchases.isConfigured
        }
    }
}
