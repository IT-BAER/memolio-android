package com.baer.memolio.core.billing

import android.app.Activity
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.PurchasesTransactionException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.PurchaseParams
import javax.inject.Inject

/**
 * Real RevenueCat wiring (purchases-android 8.x). This is the ONLY class that imports
 * com.revenuecat.purchases, so an SDK drift is contained here. Device/network only:
 * requires Google Play Billing + a configured RevenueCat project. All branching beyond
 * mapping the SDK's userCancelled flag is trivial pass-through, verified end-to-end via
 * EntitlementRepositoryTest against the fake RevenueCatClient.
 */
class RevenueCatClientImpl @Inject constructor() : RevenueCatClient {

    private val purchases: Purchases get() = Purchases.sharedInstance

    override suspend fun isEntitled(): Boolean {
        val info = purchases.awaitCustomerInfo(CacheFetchPolicy.FETCH_CURRENT)
        return info.entitlements[PRO_ENTITLEMENT_ID]?.isActive == true
    }

    override suspend fun loadOfferingTitles(): List<String> {
        val offerings = purchases.awaitOfferings()
        val current = offerings.current ?: return emptyList()
        return current.availablePackages.map { it.product.title }
    }

    override suspend fun purchase(activity: Activity): PurchaseResult {
        val offerings = purchases.awaitOfferings()
        val pkg = offerings.current?.availablePackages?.firstOrNull()
            ?: return PurchaseResult.Error("No Pro package available")
        return try {
            val result = purchases.awaitPurchase(PurchaseParams.Builder(activity, pkg).build())
            val active = result.customerInfo.entitlements[PRO_ENTITLEMENT_ID]?.isActive == true
            if (active) PurchaseResult.Success else PurchaseResult.Error("Purchase did not grant Pro")
        } catch (e: PurchasesTransactionException) {
            if (e.userCancelled) PurchaseResult.Cancelled
            else PurchaseResult.Error(e.message ?: "Purchase failed")
        }
    }

    override suspend fun restore(): RestoreResult {
        return try {
            val info = purchases.awaitRestore()
            if (info.entitlements[PRO_ENTITLEMENT_ID]?.isActive == true) RestoreResult.Success
            else RestoreResult.Error("No Pro purchase found to restore")
        } catch (e: PurchasesException) {
            RestoreResult.Error(e.message ?: "Restore failed")
        }
    }

    companion object {
        /** The RevenueCat entitlement identifier configured in the dashboard. */
        const val PRO_ENTITLEMENT_ID = "pro"
    }
}
