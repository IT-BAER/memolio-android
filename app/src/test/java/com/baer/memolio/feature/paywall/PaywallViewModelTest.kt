package com.baer.memolio.feature.paywall

import android.app.Activity
import com.baer.memolio.core.billing.EntitlementRepository
import com.baer.memolio.core.billing.PurchaseResult
import com.baer.memolio.core.billing.RestoreResult
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PaywallViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    @Before fun setMain() = Dispatchers.setMain(dispatcher)
    @After fun reset() = Dispatchers.resetMain()

    private class FakeEntitlement(initialPro: Boolean = false) : EntitlementRepository {
        val pro = MutableStateFlow(initialPro)
        var refreshCalls = 0
        override val isPro: Flow<Boolean> = pro
        override suspend fun refresh() { refreshCalls++; pro.value = true }
        override suspend fun purchase(activity: Activity): PurchaseResult = PurchaseResult.Success
        override suspend fun restore(): RestoreResult = RestoreResult.Success
    }

    @Test
    fun onCompletedRefreshesEntitlement() = runTest {
        val ent = FakeEntitlement()
        val vm = PaywallViewModel(ent)
        vm.onCompleted()
        assertThat(ent.refreshCalls).isEqualTo(1)
        assertThat(ent.pro.value).isTrue()
    }
}
