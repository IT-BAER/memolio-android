package com.baer.memolio.core.billing

/**
 * Thin internal seam over the RevenueCat SDK. The ONLY place that ever touches
 * com.revenuecat.purchases is RevenueCatClientImpl (a later task). EntitlementRepository
 * and every test depend on this interface + fakes, so SDK drift is contained to one Impl.
 */
interface RevenueCatClient {
    suspend fun isEntitled(): Boolean
}
