package com.baer.memolio.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.Symbol

/**
 * Section header — large title with an optional subtitle, and an optional
 * trailing [action] slot, baseline-aligned. The standard head atop every
 * Manage section's detail pane.
 */
@Composable
fun SectionHead(
    title: String,
    modifier: Modifier = Modifier,
    sub: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth().padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(title, color = MemolioColors.TextPrimary, style = MemolioType.h2)
            if (sub != null) {
                Text(
                    sub,
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.sm,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        if (action != null) action()
    }
}

/**
 * Pro upsell row — a glass card with a lock glyph, the feature name + Pro badge,
 * a reassuring one-liner, and a "See Pro" button that opens the paywall. Shown
 * above gated sections when the user is not Pro.
 */
@Composable
fun ProLock(
    feature: String,
    onUpsell: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val blurb = "A single purchase switches this on for good, right on the tablet."
    MemolioCard(modifier = modifier.fillMaxWidth(), variant = CardVariant.Glass) {
        // The design is lock · (title + Pro badge / blurb) · CTA, side-by-side. That fits the
        // full-width detail panes (Library/Appliance/Wallpaper). The Playlist "Active albums"
        // half-column is far too narrow for three cells in a row, so there it stacks instead of
        // collapsing the text to one glyph per line.
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 400.dp) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Symbol("lock", size = 30.sp, tint = MemolioColors.AmberSoft, modifier = Modifier.padding(end = 16.dp))
                    Column(Modifier.weight(1f).padding(end = 14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                            Text(
                                feature,
                                color = MemolioColors.TextPrimary,
                                style = MemolioType.body,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(end = 10.dp),
                            )
                            MemolioBadge("Pro", tone = BadgeTone.Pro)
                        }
                        Text(blurb, color = MemolioColors.TextSecondary, style = MemolioType.sm)
                    }
                    MemolioButton("See Pro", onUpsell, variant = ButtonVariant.Secondary, size = ButtonSize.Sm)
                }
            } else {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Symbol("lock", size = 26.sp, tint = MemolioColors.AmberSoft)
                        Spacer(Modifier.weight(1f))
                        MemolioBadge("Pro", tone = BadgeTone.Pro)
                    }
                    Text(feature, color = MemolioColors.TextPrimary, style = MemolioType.body, fontWeight = FontWeight.Medium)
                    Text(blurb, color = MemolioColors.TextSecondary, style = MemolioType.sm)
                    MemolioButton("See Pro", onUpsell, variant = ButtonVariant.Secondary, size = ButtonSize.Sm, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
