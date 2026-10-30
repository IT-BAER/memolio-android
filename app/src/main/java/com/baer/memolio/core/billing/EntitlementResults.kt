package com.baer.memolio.core.billing

import androidx.annotation.StringRes
import com.baer.memolio.R

/**
 * Localizable billing failure reasons. The billing layer is a non-Compose data layer, so
 * it must NOT embed English prose; it returns one of these codes and the paywall UI maps
 * [messageRes] to a localized string at display time (current app locale).
 */
enum class BillingError(@StringRes val messageRes: Int) {
    NO_RESTORE(R.string.billing_error_no_restore),
}
