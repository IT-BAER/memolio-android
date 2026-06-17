package com.baer.memolio.core.server

import com.baer.memolio.core.datastore.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Reactive upload URL for the QR/pairing UI. Emits `http://<ip:port>/?t=<token>` when
 * both an address and a non-empty token are present, else null.
 */
interface UploadUrlProvider {
    val uploadUrl: Flow<String?>
}

class UploadUrlProviderImpl(
    address: StateFlow<String?>,
    token: Flow<String>
) : UploadUrlProvider {

    override val uploadUrl: Flow<String?> = combine(address, token) { addr, t ->
        if (addr.isNullOrEmpty() || t.isEmpty()) null else "http://$addr/?t=$t"
    }

    /** Hilt entry point: wires [FrameServer.address] + the settings token. */
    @Inject
    constructor(server: FrameServer, settings: SettingsRepository) : this(
        server.address,
        settings.appSettings.map { it.uploadToken }
    )
}
