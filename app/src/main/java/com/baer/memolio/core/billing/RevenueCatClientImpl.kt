package com.baer.memolio.core.billing

import android.app.Activity
import android.content.Context
import com.baer.memolio.BuildConfig
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.PurchasesTransactionException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.PurchaseParams
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Real RevenueCat wiring (purchases-android 8.x). This is the ONLY class that imports
 * com.revenuecat.purchases, so an SDK drift is contained here. Device/network only:
 * requires Google Play Billing + a configured RevenueCat project. All branching beyond
 * mapping the SDK's userCancelled flag is trivial pass-through, verified end-to-end via
 * EntitlementRepositoryTest against the fake RevenueCatClient.
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

    override suspend fun purchase(activity: Activity): PurchaseResult {
        val offerings = purchases.awaitOfferings()
        val pkg = offerings.current?.availablePackages?.firstOrNull()
            ?: return PurchaseResult.Error(BillingError.NO_PACKAGE)
        return try {
            val result = purchases.awaitPurchase(PurchaseParams.Builder(activity, pkg).build())
            val active = result.customerInfo.entitlements[PRO_ENTITLEMENT_ID]?.isActive == true
            if (active) PurchaseResult.Success else PurchaseResult.Error(BillingError.NOT_GRANTED)
        } catch (e: PurchasesTransactionException) {
            if (e.userCancelled) PurchaseResult.Cancelled
            else PurchaseResult.Error(BillingError.PURCHASE_FAILED)
        }
    }

    override suspend fun restore(): RestoreResult {
        return try {
            val info = purchases.awaitRestore()
            if (info.entitlements[PRO_ENTITLEMENT_ID]?.isActive == true) RestoreResult.Success
            else RestoreResult.Error(BillingError.NO_RESTORE)
        } catch (e: PurchasesException) {
            RestoreResult.Error(BillingError.RESTORE_FAILED)
        }
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
