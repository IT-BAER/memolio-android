package com.baer.memolio.feature.manage.addphotos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.R
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
        SectionHead(title = stringResource(R.string.addphotos_title), sub = stringResource(R.string.addphotos_subtitle))
        if (url == null) {
            Text(
                stringResource(R.string.addphotos_server_starting),
                color = MemolioColors.TextSecondary,
                style = MemolioType.body
            )
        } else {
            // QR is intrinsic-width beside the instructions in landscape; in portrait
            // (narrow pane) the instructions stack beneath the QR instead of being crushed.
            val qrCard = @Composable {
                MemolioCard(variant = CardVariant.Raised, contentPadding = PaddingValues(24.dp)) {
                    Box(Modifier.size(220.dp)) {
                        QrImage(text = url, encoder = qrEncoder)
                    }
                }
            }
            val instructions = @Composable { instModifier: Modifier ->
                Column(instModifier) {
                    Text(stringResource(R.string.addphotos_point_camera), color = MemolioColors.TextPrimary, style = MemolioType.h3)
                    Text(
                        stringResource(R.string.addphotos_instructions),
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
                        text = stringResource(R.string.addphotos_rotate_token),
                        onClick = viewModel::rotateToken,
                        variant = ButtonVariant.Ghost,
                        icon = "autorenew",
                        maxLines = 2
                    )
                }
            }
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth < 560.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                        qrCard()
                        instructions(Modifier.fillMaxWidth())
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        qrCard()
                        instructions(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
