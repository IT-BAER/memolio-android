package com.baer.memolio.feature.paywall

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.baer.memolio.core.billing.BillingError
import com.baer.memolio.core.billing.EntitlementRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
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
        override suspend fun setPro(active: Boolean) { pro.value = active }
    }

    @Test
    fun openingThePaywallRefreshesPro() = runTest {
        val ent = FakeEntitlement()
        PaywallViewModel(ent)
        assertThat(ent.refreshCalls).isEqualTo(1)
        assertThat(ent.pro.value).isTrue()
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
        assertThat(vm.message.value).isNull()
    }

    @Test
    fun pendingPurchaseIsRemembered() = runTest {
        val ent = FakeEntitlement()
        val vm = PaywallViewModel(ent)
        vm.onPurchasePending()
        assertThat(ent.pendingMarks).isEqualTo(1)
    }

    /** setPro suspends until [io] completes, like the DataStore write on a device. */
    private class SlowEntitlement : EntitlementRepository {
        val pro = MutableStateFlow(false)
        val io = CompletableDeferred<Unit>()
        override val isPro: Flow<Boolean> = pro
        override suspend fun refresh() {}
        override suspend fun refreshOnAppStart() {}
        override suspend fun markPurchasePending() {}
        override suspend fun setPro(active: Boolean) { io.await(); pro.value = active }
    }

    private fun openPaywall(ent: EntitlementRepository, store: ViewModelStore): PaywallViewModel =
        ViewModelProvider(store, viewModelFactory { initializer { PaywallViewModel(ent) } })[PaywallViewModel::class.java]

    // RevenueCatUI dismisses the dialog right after a purchase or restore; the screen pops
    // and clears the ViewModel before the entitlement write finishes.
    @Test
    fun purchaseUnlocksProWhenThePaywallClosesAtOnce() = runTest {
        val ent = SlowEntitlement()
        val store = ViewModelStore()
        val vm = openPaywall(ent, store)
        vm.onCompleted(active = true)
        store.clear()
        ent.io.complete(Unit)
        assertThat(ent.pro.value).isTrue()
    }

    @Test
    fun restoreUnlocksProWhenThePaywallClosesAtOnce() = runTest {
        val ent = SlowEntitlement()
        val store = ViewModelStore()
        val vm = openPaywall(ent, store)
        vm.onRestoreCompleted(active = true)
        store.clear()
        ent.io.complete(Unit)
        assertThat(ent.pro.value).isTrue()
    }
}
