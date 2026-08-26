package com.baer.memolio.feature.manage.addphotos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.server.UploadUrlProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddPhotosUiState(val uploadUrl: String? = null, val serverRunning: Boolean = false)

@HiltViewModel
class AddPhotosViewModel @Inject constructor(
    uploadUrlProvider: UploadUrlProvider,
    private val settings: SettingsRepository
) : ViewModel() {

    val state: StateFlow<AddPhotosUiState> =
        combine(uploadUrlProvider.uploadUrl, uploadUrlProvider.serverRunning) { url, running ->
            AddPhotosUiState(uploadUrl = url, serverRunning = running)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddPhotosUiState())

    fun rotateToken() = viewModelScope.launch { settings.rotateToken() }
}
