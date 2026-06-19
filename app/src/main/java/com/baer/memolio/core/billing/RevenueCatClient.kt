package com.baer.memolio.core.billing

import android.app.Activity

/**
 * Thin internal seam over the RevenueCat SDK. The ONLY place that ever touches
 * com.revenuecat.purchases is RevenueCatClientImpl (a later task). EntitlementRepository
 * and every test depend on this interface + fakes, so SDK drift is contained to one Impl.
 */
interface RevenueCatClient {
    suspend fun isEntitled(): Boolean
    suspend fun loadOfferingTitles(): List<String>
    suspend fun purchase(activity: Activity): PurchaseResult
    suspend fun restore(): RestoreResult
}
