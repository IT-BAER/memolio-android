package com.baer.memolio.feature.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.billing.EntitlementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The RevenueCatUI PaywallDialog drives purchase/restore itself. This ViewModel's only
 * job is to refresh the sticky-true entitlement cache after the dialog reports a completed
 * purchase or restore (via PaywallListener). refresh() reads CustomerInfo through the
 * RevenueCatClient seam and sets proUnlocked when the "pro" entitlement is active.
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val entitlement: EntitlementRepository
) : ViewModel() {
    fun onCompleted() = viewModelScope.launch { entitlement.refresh() }
}
