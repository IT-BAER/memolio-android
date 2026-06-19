package com.baer.memolio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.baer.memolio.core.datastore.AppSettings
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.ui.MemolioTheme
import com.baer.memolio.feature.frame.FrameRoute
import com.baer.memolio.feature.manage.ManageScaffold
import com.baer.memolio.feature.onboard.OnboardScreen
import com.baer.memolio.navigation.MemolioNavHost
import com.baer.memolio.navigation.startDestination
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemolioTheme {
                val settings by settingsRepository.appSettings.collectAsState(initial = AppSettings())
                Surface(modifier = Modifier.fillMaxSize()) {
                    MemolioNavHost(
                        start = startDestination(settings.onboardingComplete),
                        frameContent = { onOpenManage -> FrameRoute(onOpenManage = onOpenManage) },
                        manageContent = { ManageScaffold() },
                        onboardContent = { OnboardScreen() }
                    )
                }
            }
        }
    }
}
