package com.baer.memolio.feature.paywall

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.R
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.Symbol
import com.baer.memolio.core.ui.component.BadgeTone
import com.baer.memolio.core.ui.component.ButtonSize
import com.baer.memolio.core.ui.component.ButtonVariant
import com.baer.memolio.core.ui.component.CardVariant
import com.baer.memolio.core.ui.component.IconButtonSize
import com.baer.memolio.core.ui.component.IconButtonVariant
import com.baer.memolio.core.ui.component.MemolioBadge
import com.baer.memolio.core.ui.component.MemolioButton
import com.baer.memolio.core.ui.component.MemolioCard
import com.baer.memolio.core.ui.component.MemolioIconButton
import com.baer.memolio.core.ui.component.MemolioWordmark
import com.baer.memolio.core.ui.component.WordmarkTone

private data class Perk(val icon: String, @StringRes val titleRes: Int, @StringRes val descRes: Int)

private val PERKS = listOf(
    Perk("photo_library", R.string.paywall_perk_albums_title, R.string.paywall_perk_albums_desc),
    Perk("tune", R.string.paywall_perk_appliance_title, R.string.paywall_perk_appliance_desc),
    Perk("wallpaper", R.string.paywall_perk_wallpaper_title, R.string.paywall_perk_wallpaper_desc),
)

@Composable
fun PaywallScreen(
    onClose: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(MemolioColors.Ink000.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        // Cap the card to the viewport (leaving a 24dp margin top+bottom) so on short
        // landscape tablets the inner Column scrolls instead of the whole card clipping.
        MemolioCard(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxWidth()
                .heightIn(max = maxHeight - 48.dp)
                .padding(24.dp),
            variant = CardVariant.Raised,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(48.dp)
        ) {
            // Close affordance, top-right.
            MemolioIconButton(
                icon = "close",
                contentDescription = stringResource(R.string.action_close),
                onClick = onClose,
                variant = IconButtonVariant.Bare,
                size = IconButtonSize.Sm,
                modifier = Modifier.align(Alignment.TopEnd)
            )
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MemolioWordmark(tone = WordmarkTone.Solid, size = 22.sp)
                    MemolioBadge(stringResource(R.string.badge_pro), tone = BadgeTone.Pro)
                }

                when {
                    state.isPro -> {
                        Text(
                            stringResource(R.string.paywall_unlocked),
                            color = MemolioColors.TextSecondary,
                            style = MemolioType.body,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )
                        MemolioButton(stringResource(R.string.action_done), onClose, variant = ButtonVariant.Primary, size = ButtonSize.Lg)
                    }

                    state.offline -> {
                        Text(
                            stringResource(R.string.paywall_offline),
                            color = MemolioColors.TextSecondary,
                            style = MemolioType.body,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )
                        MemolioButton(stringResource(R.string.action_maybe_later), onClose, variant = ButtonVariant.Quiet)
                    }

                    else -> {
                        Text(
                            stringResource(R.string.paywall_pitch),
                            color = MemolioColors.TextSecondary,
                            style = MemolioType.body,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 26.dp)) {
                            PERKS.forEach { p ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Design: a bare 26px teal glyph (no tinted tile), top-aligned to the title.
                                    Symbol(p.icon, size = 26.sp, tint = MemolioColors.Teal)
                                    Column {
                                        Text(stringResource(p.titleRes), color = MemolioColors.TextPrimary, style = MemolioType.body, fontWeight = FontWeight.Medium)
                                        Text(stringResource(p.descRes), color = MemolioColors.TextSecondary, style = MemolioType.sm)
                                    }
                                }
                            }
                        }
                        Row(
                            Modifier.padding(bottom = 20.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(stringResource(R.string.paywall_price), color = MemolioColors.TextPrimary, style = MemolioType.display)
                            Text(stringResource(R.string.paywall_price_caption), color = MemolioColors.TextTertiary, style = MemolioType.sm, modifier = Modifier.padding(bottom = 12.dp))
                        }
                        state.error?.let {
                            Text(
                                stringResource(R.string.paywall_error, stringResource(it.messageRes)),
                                color = MemolioColors.Error,
                                style = MemolioType.sm,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            MemolioButton(
                                text = stringResource(R.string.paywall_unlock_cta),
                                onClick = { activity?.let { viewModel.purchase(it) } },
                                variant = ButtonVariant.Primary,
                                size = ButtonSize.Lg,
                                icon = "lock_open",
                                enabled = !state.loading && activity != null,
                                modifier = Modifier.weight(1f)
                            )
                            MemolioButton(
                                text = stringResource(R.string.paywall_restore),
                                onClick = { viewModel.restore() },
                                variant = ButtonVariant.Quiet,
                                enabled = !state.loading
                            )
                        }
                        if (state.loading) {
                            CircularProgressIndicator(
                                color = MemolioColors.Teal,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
