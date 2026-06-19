package com.baer.memolio.feature.manage.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baer.memolio.core.data.PhotoRepository
import com.baer.memolio.core.datastore.SettingsRepository
import com.baer.memolio.core.di.IoDispatcher
import com.baer.memolio.core.model.Photo
import com.baer.memolio.core.storage.FileStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class StorageUiState(
    val usedBytes: Long = 0L,
    val trash: List<Photo> = emptyList(),
    val autoCleanup: Boolean = false
)

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val settings: SettingsRepository,
    private val fileStorage: FileStorage,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    // Test-only constructor: accepts a clock stub for parity with other sections.
    // `now` is not currently used by Storage logic, so it is intentionally ignored.
    internal constructor(
        photoRepository: PhotoRepository,
        settings: SettingsRepository,
        fileStorage: FileStorage,
        ioDispatcher: CoroutineDispatcher,
        @Suppress("UNUSED_PARAMETER") now: () -> Long
    ) : this(photoRepository, settings, fileStorage, ioDispatcher)

    private val usedBytes = MutableStateFlow(0L)

    init {
        viewModelScope.launch { usedBytes.value = withContext(ioDispatcher) { fileStorage.usedBytes() } }
    }

    val state: StateFlow<StorageUiState> =
        combine(
            usedBytes,
            photoRepository.observeTrash(),
            settings.appSettings.map { it.autoCleanup }
        ) { bytes, trash, autoCleanup ->
            StorageUiState(usedBytes = bytes, trash = trash, autoCleanup = autoCleanup)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StorageUiState())

    fun restore(id: String) = viewModelScope.launch { photoRepository.restore(id) }

    /** Empties the whole trash regardless of age (spec section 7 "empty"). */
    fun emptyTrash() = viewModelScope.launch { photoRepository.purgeTrashOlderThan(Long.MAX_VALUE) }

    fun setAutoCleanup(value: Boolean) = viewModelScope.launch { settings.setAutoCleanup(value) }

    fun refreshUsage() = viewModelScope.launch {
        usedBytes.value = withContext(ioDispatcher) { fileStorage.usedBytes() }
    }
}
