package com.baer.memolio.feature.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.billing.BillingError
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.billing.PurchaseResult
import com.baer.memolio.core.billing.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Connectivity seam: emits true when the device has internet. Faked in unit tests. */
interface Connectivity {
    val isOnline: Flow<Boolean>
}

data class PaywallUiState(
    val isPro: Boolean = false,
    val offline: Boolean = false,
    val loading: Boolean = false,
    val offerings: List<String> = emptyList(),
    val error: BillingError? = null
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val entitlement: EntitlementRepository,
    connectivity: Connectivity,
    /** Suspending offerings loader (RevenueCat-backed in production, faked in tests). */
    private val loadOfferings: @JvmSuppressWildcards suspend () -> List<String>
) : ViewModel() {

    private val loading = MutableStateFlow(false)
    private val offerings = MutableStateFlow<List<String>>(emptyList())
    private val error = MutableStateFlow<BillingError?>(null)

    val state: StateFlow<PaywallUiState> =
        combine(
            entitlement.isPro,
            connectivity.isOnline,
            loading,
            offerings,
            error
        ) { isPro, online, isLoading, offers, err ->
            PaywallUiState(
                isPro = isPro,
                offline = !online && !isPro,
                loading = isLoading,
                offerings = offers,
                error = err
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PaywallUiState())

    init {
        viewModelScope.launch {
            runCatching { offerings.value = loadOfferings() }
                .onFailure { error.update { it ?: BillingError.LOAD_FAILED } }
        }
    }

    fun purchase(activity: Activity) = viewModelScope.launch {
        loading.value = true
        error.value = null
        when (val result = entitlement.purchase(activity)) {
            is PurchaseResult.Success -> Unit            // isPro flips via the flow
            is PurchaseResult.Cancelled -> Unit          // silent, no error
            is PurchaseResult.Error -> error.value = result.error
        }
        loading.value = false
    }

    fun restore() = viewModelScope.launch {
        loading.value = true
        error.value = null
        when (val result = entitlement.restore()) {
            is RestoreResult.Success -> Unit
            is RestoreResult.Cancelled -> Unit
            is RestoreResult.Error -> error.value = result.error
        }
        loading.value = false
    }
}
