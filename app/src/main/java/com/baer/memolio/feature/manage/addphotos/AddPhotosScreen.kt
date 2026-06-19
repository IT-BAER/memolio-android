package com.baer.memolio.feature.manage.addphotos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baer.memolio.core.media.QrEncoder

@Composable
fun AddPhotosScreen(
    viewModel: AddPhotosViewModel = hiltViewModel(),
    qrEncoder: QrEncoder = QrEncoder(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        val url = state.uploadUrl
        if (url == null) {
            Text("Server starting, connect the tablet to Wi-Fi.")
        } else {
            QrImage(text = url, encoder = qrEncoder)
            Text(url, modifier = Modifier.padding(top = 16.dp))
        }
        Button(onClick = viewModel::rotateToken, modifier = Modifier.padding(top = 16.dp)) {
            Text("Rotate token (invalidate old QR)")
        }
    }
}
