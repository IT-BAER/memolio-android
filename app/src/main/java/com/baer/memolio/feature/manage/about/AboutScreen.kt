package com.baer.memolio.feature.manage.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baer.memolio.BuildConfig

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Memolio")
        Text("Version ${BuildConfig.VERSION_NAME}")
        Text("A fully-offline digital photo frame. No cloud, no accounts.")
    }
}
