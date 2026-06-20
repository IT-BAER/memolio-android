package com.baer.memolio.feature.manage.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baer.memolio.BuildConfig
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.component.BadgeTone
import com.baer.memolio.core.ui.component.CardVariant
import com.baer.memolio.core.ui.component.MemolioBadge
import com.baer.memolio.core.ui.component.MemolioCard
import com.baer.memolio.core.ui.component.MemolioWordmark
import com.baer.memolio.core.ui.component.SectionHead
import com.baer.memolio.core.ui.component.WordmarkTone

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(modifier) {
        SectionHead(title = "About")
        MemolioCard(modifier = Modifier.widthIn(max = 480.dp), variant = CardVariant.Surface) {
            Column {
                MemolioWordmark(tone = WordmarkTone.Solid, size = 26.sp)
                Text(
                    "A spare tablet, given a second life on your shelf. Memolio shows the " +
                        "people and places worth looking up for, and keeps every photo in its " +
                        "own folder on this device, reachable only over your home Wi-Fi.",
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.body,
                    modifier = Modifier.padding(top = 14.dp)
                )
                Row(
                    Modifier.padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MemolioBadge("v${BuildConfig.VERSION_NAME}", tone = BadgeTone.Neutral)
                    MemolioBadge("Offline-first", tone = BadgeTone.Teal)
                }
            }
        }
    }
}
