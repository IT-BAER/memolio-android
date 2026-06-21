package com.baer.memolio.feature.paywall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.core.billing.RevenueCatClientImpl
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package as RcPackage
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener

/**
 * Renders the RevenueCat dashboard-designed paywall (offering "default") as a modal dialog.
 * Copy, perks, price, and CTA all live in the RC dashboard. Purchase/restore are driven by
 * the SDK; on completion PaywallListener routes to onCompleted() which refreshes the
 * sticky-true proUnlocked cache. setRequiredEntitlementIdentifier auto-dismisses once Pro.
 *
 * The Purchases.isConfigured guard is crash-safety ONLY: debug builds run without a
 * REVENUECAT_API_KEY so Purchases is unconfigured and PaywallDialog would crash. In any
 * configured build the raw RC paywall is shown (it owns its own loading/error UI).
 */
@Composable
fun PaywallScreen(
    onClose: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    if (!Purchases.isConfigured) {
        LaunchedEffect(Unit) { onClose() }
        return
    }
    PaywallDialog(
        PaywallDialogOptions.Builder()
            .setRequiredEntitlementIdentifier(RevenueCatClientImpl.PRO_ENTITLEMENT_ID)
            .setDismissRequest { onClose() }
            .setListener(object : PaywallListener {
                override fun onPurchaseStarted(rcPackage: RcPackage) {}

                override fun onPurchaseCompleted(
                    customerInfo: CustomerInfo,
                    storeTransaction: StoreTransaction
                ) {
                    viewModel.onCompleted()
                }

                override fun onPurchaseError(error: PurchasesError) {}

                override fun onPurchaseCancelled() {}

                override fun onRestoreStarted() {}

                override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                    viewModel.onCompleted()
                }

                override fun onRestoreError(error: PurchasesError) {}
            })
            .build()
    )
}
