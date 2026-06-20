package com.baer.memolio.feature.manage.addphotos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.core.media.QrEncoder
import com.baer.memolio.core.ui.MemolioColors
import com.baer.memolio.core.ui.MemolioType
import com.baer.memolio.core.ui.Symbol
import com.baer.memolio.core.ui.component.ButtonVariant
import com.baer.memolio.core.ui.component.CardVariant
import com.baer.memolio.core.ui.component.MemolioButton
import com.baer.memolio.core.ui.component.MemolioCard
import com.baer.memolio.core.ui.component.SectionHead

@Composable
fun AddPhotosScreen(
    viewModel: AddPhotosViewModel = hiltViewModel(),
    qrEncoder: QrEncoder = QrEncoder(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val url = state.uploadUrl
    Column(modifier.verticalScroll(rememberScrollState())) {
        SectionHead(title = "Add photos", sub = "Scan from any phone on the same Wi-Fi")
        if (url == null) {
            Text(
                "Server starting. Connect the tablet to Wi-Fi.",
                color = MemolioColors.TextSecondary,
                style = MemolioType.body
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.CenterVertically) {
                MemolioCard(variant = CardVariant.Raised, contentPadding = PaddingValues(24.dp)) {
                    Box(Modifier.size(220.dp)) {
                        QrImage(text = url, encoder = qrEncoder)
                    }
                }
                Column {
                    Text("Point a camera here", color = MemolioColors.TextPrimary, style = MemolioType.h3)
                    Text(
                        "Drop a photo and it lands on the frame within seconds. It lives in Memolio's own folder, never your tablet's gallery, and goes no further than this room.",
                        color = MemolioColors.TextSecondary,
                        style = MemolioType.body,
                        modifier = Modifier.padding(top = 8.dp).widthIn(max = 420.dp)
                    )
                    Row(
                        Modifier
                            .padding(vertical = 18.dp)
                            .background(MemolioColors.SurfaceGlass, RoundedCornerShape(10.dp))
                            .border(1.dp, MemolioColors.BorderSoft, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Symbol("link", size = 18.sp, tint = MemolioColors.TextTertiary)
                        Text(url, color = MemolioColors.TextTertiary, style = MemolioType.sm)
                    }
                    MemolioButton(
                        text = "Rotate token (invalidate old QR)",
                        onClick = viewModel::rotateToken,
                        variant = ButtonVariant.Ghost,
                        icon = "autorenew",
                        maxLines = 2
                    )
                }
            }
        }
    }
}
