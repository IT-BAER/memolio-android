package com.baer.memolio.feature.paywall

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.core.billing.RevenueCatClientImpl
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package as RcPackage
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener

/**
 * Renders the RevenueCat dashboard-designed paywall (offering "default") as a modal dialog.
 * Copy, perks, price, and CTA all live in the RC dashboard. Purchase/restore are driven by
 * the SDK, which also shows its own error dialog; PaywallListener routes completions to the
 * ViewModel, which stores the reported Pro state in the cached proUnlocked flag.
 *
 * Billing is configured here on first use. A build without a RevenueCat key (debug) closes at
 * once. When RevenueCat already reports Pro, PaywallDialog never opens, so the screen closes
 * itself as soon as the refreshed flag turns true.
 */
@Composable
fun PaywallScreen(
    onClose: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    // Close at most once: the dialog's own dismiss and the Pro check can both fire.
    var closed by remember { mutableStateOf(false) }
    val close = {
        if (!closed) {
            closed = true
            onClose()
        }
    }
    val billingReady = remember { RevenueCatClientImpl.ensureConfigured(context) }
    if (!billingReady) {
        LaunchedEffect(Unit) { close() }
        return
    }
    val isPro by viewModel.isPro.collectAsState(initial = false)
    LaunchedEffect(isPro) { if (isPro) close() }
    val message by viewModel.message.collectAsState()
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it.messageRes, Toast.LENGTH_LONG).show()
            viewModel.onMessageShown()
        }
    }
    PaywallDialog(
        PaywallDialogOptions.Builder()
            .setRequiredEntitlementIdentifier(RevenueCatClientImpl.PRO_ENTITLEMENT_ID)
            .setDismissRequest { close() }
            .setListener(object : PaywallListener {
                override fun onPurchaseStarted(rcPackage: RcPackage) {}

                override fun onPurchaseCompleted(
                    customerInfo: CustomerInfo,
                    storeTransaction: StoreTransaction
                ) {
                    viewModel.onCompleted(active = customerInfo.hasPro())
                }

                // The SDK shows the error itself; a pending payment is remembered for the start check.
                override fun onPurchaseError(error: PurchasesError) {
                    if (error.code == PurchasesErrorCode.PaymentPendingError) viewModel.onPurchasePending()
                }

                override fun onPurchaseCancelled() {}

                override fun onRestoreStarted() {}

                override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                    viewModel.onRestoreCompleted(active = customerInfo.hasPro())
                }

                // The SDK shows the error itself.
                override fun onRestoreError(error: PurchasesError) {}
            })
            .build()
    )
}

private fun CustomerInfo.hasPro() =
    entitlements[RevenueCatClientImpl.PRO_ENTITLEMENT_ID]?.isActive == true
