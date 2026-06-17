package com.baer.memolio.core.server

import com.baer.memolio.core.datastore.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Production [TokenProvider] honoring shared-contract **addendum C**: the token used by
 * [FrameServer]'s 403 gate must reflect [SettingsRepository.rotateToken] immediately, with
 * no restart. We hold the latest token in a `@Volatile` field kept in sync by *collecting*
 * `SettingsRepository.appSettings` (a hot subscription), NOT a one-shot `runBlocking` read.
 *
 * [current] is therefore a non-blocking volatile read on the Ktor request thread, and the
 * moment Phase 4's rotate button persists a new token the collector updates [cached] so the
 * next request rejects the old link.
 *
 * Construct via [SettingsTokenProvider.start] (production) which begins the collection in the
 * supplied [scope]; the primary constructor takes the token [Flow] directly so it is unit-
 * testable without a real DataStore.
 */
class SettingsTokenProvider private constructor(
    scope: CoroutineScope,
    tokenFlow: Flow<String>
) : TokenProvider {

    @Volatile
    private var cached: String = ""

    init {
        scope.launch {
            tokenFlow.collect { cached = it }
        }
    }

    override fun current(): String = cached

    companion object {
        /** Begins syncing from `settings.appSettings.uploadToken` in [scope]. */
        fun start(scope: CoroutineScope, settings: SettingsRepository): SettingsTokenProvider =
            SettingsTokenProvider(scope, settings.appSettings.map { it.uploadToken })

        /** Test/seam entry point: sync from any token [Flow]. */
        fun startFrom(scope: CoroutineScope, tokenFlow: Flow<String>): SettingsTokenProvider =
            SettingsTokenProvider(scope, tokenFlow)
    }
}
