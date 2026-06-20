package com.baer.memolio.core.server

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Outcome of a single inbound upload, broadcast for UI feedback. */
enum class UploadOutcome { ADDED, DUPLICATE, REJECTED }

/**
 * App-singleton bus the embedded server publishes each upload outcome to, so the UI can
 * surface a transient "photo added" banner regardless of which screen is visible. The
 * server side only writes; the activity collects. Buffered + drop-oldest so a burst of
 * uploads never suspends the request handler if nothing is currently collecting.
 */
@Singleton
class UploadEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<UploadOutcome>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<UploadOutcome> = _events.asSharedFlow()

    fun publish(outcome: UploadOutcome) {
        _events.tryEmit(outcome)
    }
}
