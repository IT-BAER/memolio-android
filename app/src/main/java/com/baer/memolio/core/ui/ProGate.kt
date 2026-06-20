package com.baer.memolio.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.billing.ProFeature
import com.baer.memolio.core.ui.component.ProLock

/**
 * Renders [unlocked] when [isPro] is true, else [locked]. The default [locked] slot is a
 * compact upsell tile (feature title + blurb + "Unlock" button calling [onUpsell], which
 * the host wires to open the paywall). Pass a custom [locked] for inline lock badges.
 */
@Composable
fun ProGate(
    feature: ProFeature,
    isPro: Boolean,
    onUpsell: () -> Unit,
    modifier: Modifier = Modifier,
    locked: @Composable () -> Unit = { DefaultUpsell(feature, onUpsell) },
    unlocked: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        if (isPro) unlocked() else locked()
    }
}

@Composable
private fun DefaultUpsell(feature: ProFeature, onUpsell: () -> Unit) {
    ProLock(feature = stringResource(feature.lockLabelRes), onUpsell = onUpsell)
}

/**
 * Convenience: collect [EntitlementRepository.isPro] as Compose state (defaults to false
 * before the first emission). Use at a screen root, then pass the boolean into [ProGate].
 */
@Composable
fun rememberEntitlement(entitlement: EntitlementRepository): Boolean {
    val isPro by entitlement.isPro.collectAsState(initial = false)
    return isPro
}
