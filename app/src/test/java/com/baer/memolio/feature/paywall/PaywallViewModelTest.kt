package com.baer.memolio.feature.paywall

import com.baer.memolio.core.billing.BillingError
import com.baer.memolio.core.billing.EntitlementRepository
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
        var pendingMarks = 0
        override val isPro: Flow<Boolean> = pro
        override suspend fun refresh() { refreshCalls++; pro.value = true }
        override suspend fun refreshOnAppStart() {}
        override suspend fun markPurchasePending() { pendingMarks++ }
    }

    @Test
    fun openingThePaywallRefreshesPro() = runTest {
        val ent = FakeEntitlement()
        PaywallViewModel(ent)
        assertThat(ent.refreshCalls).isEqualTo(1)
        assertThat(ent.pro.value).isTrue()
    }

    @Test
    fun onCompletedRefreshesEntitlement() = runTest {
        val ent = FakeEntitlement()
        val vm = PaywallViewModel(ent)
        vm.onCompleted()
        assertThat(ent.refreshCalls).isEqualTo(2)
    }

    @Test
    fun restoreWithoutProShowsTheNoRestoreMessageOnce() = runTest {
        val vm = PaywallViewModel(FakeEntitlement())
        vm.onRestoreCompleted(active = false)
        assertThat(vm.message.value).isEqualTo(BillingError.NO_RESTORE)
        vm.onMessageShown()
        assertThat(vm.message.value).isNull()
    }

    @Test
    fun restoreWithProShowsNoMessage() = runTest {
        val ent = FakeEntitlement()
        val vm = PaywallViewModel(ent)
        vm.onRestoreCompleted(active = true)
        assertThat(ent.refreshCalls).isEqualTo(2)
        assertThat(vm.message.value).isNull()
    }

    @Test
    fun pendingPurchaseIsRemembered() = runTest {
        val ent = FakeEntitlement()
        val vm = PaywallViewModel(ent)
        vm.onPurchasePending()
        assertThat(ent.pendingMarks).isEqualTo(1)
    }
}
