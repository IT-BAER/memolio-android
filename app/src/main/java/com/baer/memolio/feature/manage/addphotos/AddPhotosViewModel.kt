package com.baer.memolio.feature.manage.addphotos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.server.UploadUrlProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddPhotosUiState(val uploadUrl: String? = null)

@HiltViewModel
class AddPhotosViewModel @Inject constructor(
    uploadUrlProvider: UploadUrlProvider,
    private val settings: SettingsRepository
) : ViewModel() {

    val state: StateFlow<AddPhotosUiState> =
        uploadUrlProvider.uploadUrl
            .map { AddPhotosUiState(uploadUrl = it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddPhotosUiState())

    fun rotateToken() = viewModelScope.launch { settings.rotateToken() }
}
