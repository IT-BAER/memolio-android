package com.baer.memolio.feature.manage.about

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baer.memolio.BuildConfig
import com.baer.memolio.R
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.component.BadgeTone
import com.baer.memolio.core.ui.component.CardVariant
import com.baer.memolio.core.ui.component.MemolioBadge
import com.baer.memolio.core.ui.component.MemolioCard
import com.baer.memolio.core.ui.component.MemolioWordmark
import com.baer.memolio.core.ui.component.SectionHead
import com.baer.memolio.core.ui.component.WordmarkTone

private const val REPO_URL = "https://github.com/IT-BAER/memolio-android"
private const val PRIVACY_URL = "https://github.com/IT-BAER/memolio-android/blob/main/docs/privacy-policy.md"
private const val LEGAL_URL = "https://github.com/IT-BAER/memolio-android/blob/main/docs/legal-notice.md"

/**
 * Opens [url] in a browser. Lock-task mode blocks every activity outside Memolio, and a
 * repurposed tablet can have no browser at all; both cases end in a message instead of a
 * dead tap or an ActivityNotFoundException.
 */
private fun openUrl(context: Context, url: String) {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    if (activityManager?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) {
        Toast.makeText(context, R.string.about_link_blocked_by_kiosk, Toast.LENGTH_LONG).show()
        return
    }
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        .onFailure {
            Toast.makeText(context, R.string.about_link_no_browser, Toast.LENGTH_LONG).show()
        }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier) {
        SectionHead(title = stringResource(R.string.about_title))
        MemolioCard(modifier = Modifier.widthIn(max = 480.dp), variant = CardVariant.Surface) {
            Column {
                MemolioWordmark(tone = WordmarkTone.Solid, size = 26.sp)
                Text(
                    stringResource(R.string.about_body),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.body,
                    modifier = Modifier.padding(top = 14.dp)
                )
                FlowRow(
                    Modifier.padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MemolioBadge(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), tone = BadgeTone.Neutral)
                    MemolioBadge(stringResource(R.string.about_offline_first), tone = BadgeTone.Teal)
                    MemolioBadge(stringResource(R.string.about_open_source), tone = BadgeTone.Neutral)
                }
                Row(
                    Modifier
                        .padding(top = 16.dp)
                        .clickable {
                            openUrl(context, REPO_URL)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_github),
                        contentDescription = null,
                        tint = MemolioColors.TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.about_view_source),
                        color = MemolioColors.TextSecondary,
                        style = MemolioType.body,
                    )
                }
                Text(
                    stringResource(R.string.about_privacy_policy),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.body,
                    modifier = Modifier.padding(top = 12.dp).clickable {
                        openUrl(context, PRIVACY_URL)
                    }
                )
                Text(
                    stringResource(R.string.about_legal_notice),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.body,
                    modifier = Modifier.padding(top = 12.dp).clickable {
                        openUrl(context, LEGAL_URL)
                    }
                )
            }
        }
    }
}
