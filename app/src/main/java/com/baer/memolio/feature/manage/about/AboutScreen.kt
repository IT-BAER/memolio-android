package com.baer.memolio.feature.manage.about

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
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

private const val PLAY_PACKAGE = "com.baer.memolio"
private const val PLAY_URL = "https://play.google.com/store/apps/details?id=$PLAY_PACKAGE"
private const val FEEDBACK_EMAIL = "admin@it-baer.net"

/**
 * Starts [intent] outside Memolio. Lock-task mode blocks every activity outside Memolio, and a
 * repurposed tablet can lack a browser, Play Store or mail app; both cases end in a message
 * instead of a dead tap or an ActivityNotFoundException. [onMissing] runs when nothing handles it.
 */
private fun startOutside(context: Context, intent: Intent, onMissing: () -> Unit) {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    if (activityManager?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) {
        Toast.makeText(context, R.string.about_link_blocked_by_kiosk, Toast.LENGTH_LONG).show()
        return
    }
    runCatching { context.startActivity(intent) }.onFailure { onMissing() }
}

private fun toast(context: Context, @StringRes message: Int) =
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()

private fun openUrl(context: Context, url: String) =
    startOutside(context, Intent(Intent.ACTION_VIEW, Uri.parse(url))) {
        toast(context, R.string.about_link_no_browser)
    }

/** Play Store app first; a tablet without it gets the listing in the browser. */
private fun openPlayListing(context: Context) =
    startOutside(context, Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PLAY_PACKAGE"))) {
        openUrl(context, PLAY_URL)
    }

private fun shareApp(context: Context) {
    val send = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, context.getString(R.string.about_share_message, PLAY_URL))
    startOutside(context, Intent.createChooser(send, context.getString(R.string.about_share))) {
        toast(context, R.string.about_no_app)
    }
}

private fun sendFeedback(context: Context) {
    val mail = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$FEEDBACK_EMAIL"))
        .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.about_feedback_subject, BuildConfig.VERSION_NAME))
    startOutside(context, mail) { toast(context, R.string.about_no_app) }
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
                    stringResource(R.string.about_rate),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.body,
                    modifier = Modifier.padding(top = 12.dp).clickable { openPlayListing(context) }
                )
                Text(
                    stringResource(R.string.about_share),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.body,
                    modifier = Modifier.padding(top = 12.dp).clickable { shareApp(context) }
                )
                Text(
                    stringResource(R.string.about_feedback),
                    color = MemolioColors.TextSecondary,
                    style = MemolioType.body,
                    modifier = Modifier.padding(top = 12.dp).clickable { sendFeedback(context) }
                )
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
