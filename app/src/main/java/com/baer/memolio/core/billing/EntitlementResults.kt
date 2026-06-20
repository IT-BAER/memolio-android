package com.baer.memolio.core.billing

import androidx.annotation.StringRes
import com.baer.memolio.R

/**
 * Localizable billing failure reasons. The billing layer is a non-Compose data layer, so
 * it must NOT embed English prose; it returns one of these codes and the paywall UI maps
 * [messageRes] to a localized string at display time (current app locale).
 */
enum class BillingError(@StringRes val messageRes: Int) {
    NO_PACKAGE(R.string.billing_error_no_package),
    NOT_GRANTED(R.string.billing_error_not_granted),
    PURCHASE_FAILED(R.string.billing_error_purchase_failed),
    NO_RESTORE(R.string.billing_error_no_restore),
    RESTORE_FAILED(R.string.billing_error_restore_failed),
    LOAD_FAILED(R.string.billing_error_load),
}

/** Outcome of a RevenueCat purchase flow. Mirrors the contract's sealed shape. */
sealed interface PurchaseResult {
    data object Success : PurchaseResult
    data object Cancelled : PurchaseResult
    data class Error(val error: BillingError) : PurchaseResult
}

/** Outcome of a RevenueCat restore. Same shape as PurchaseResult (contract). */
sealed interface RestoreResult {
    data object Success : RestoreResult
    data object Cancelled : RestoreResult
    data class Error(val error: BillingError) : RestoreResult
}
