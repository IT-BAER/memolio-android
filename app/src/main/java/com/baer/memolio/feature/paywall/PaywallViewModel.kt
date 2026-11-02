package com.baer.memolio.feature.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.billing.BillingError
import com.baer.memolio.core.billing.EntitlementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The RevenueCatUI PaywallDialog drives purchase/restore itself and shows its own error dialog.
 * This ViewModel refreshes the cached entitlement when the paywall opens (RevenueCat may already
 * know a purchase, for example a pending one that completed later) and stores the Pro state the
 * dialog reports with a completed purchase or restore. [message] carries the one case the dialog
 * stays silent on: a restore that found no Pro purchase.
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val entitlement: EntitlementRepository
) : ViewModel() {

    val isPro: Flow<Boolean> = entitlement.isPro

    private val _message = MutableStateFlow<BillingError?>(null)
    val message: StateFlow<BillingError?> = _message.asStateFlow()

    init {
        viewModelScope.launch { entitlement.refresh() }
    }

    // The dialog closes the screen right after a purchase or restore, which clears this
    // ViewModel; NonCancellable lets the write finish anyway.
    fun onCompleted(active: Boolean) = viewModelScope.launch {
        withContext(NonCancellable) { entitlement.setPro(active) }
    }

    fun onRestoreCompleted(active: Boolean) = viewModelScope.launch {
        withContext(NonCancellable) { entitlement.setPro(active) }
        if (!active) _message.value = BillingError.NO_RESTORE
    }

    fun onPurchasePending() = viewModelScope.launch { entitlement.markPurchasePending() }

    fun onMessageShown() {
        _message.value = null
    }
}
