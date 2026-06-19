package com.baer.memolio.core.billing

/** Outcome of a RevenueCat purchase flow. Mirrors the contract's sealed shape. */
sealed interface PurchaseResult {
    data object Success : PurchaseResult
    data object Cancelled : PurchaseResult
    data class Error(val message: String) : PurchaseResult
}

/** Outcome of a RevenueCat restore. Same shape as PurchaseResult (contract). */
sealed interface RestoreResult {
    data object Success : RestoreResult
    data object Cancelled : RestoreResult
    data class Error(val message: String) : RestoreResult
}
