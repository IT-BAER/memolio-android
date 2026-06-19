package com.baer.memolio.feature.paywall

import android.app.Activity
import app.cash.turbine.test
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
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PaywallViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    @Before fun setMain() = Dispatchers.setMain(dispatcher)
    @After fun reset() = Dispatchers.resetMain()

    private val activity: Activity = Robolectric.buildActivity(Activity::class.java).get()

    private class FakeEntitlement(
        initialPro: Boolean = false,
        var offeringTitles: List<String> = listOf("Memolio Pro"),
        var purchaseResult: PurchaseResult = PurchaseResult.Success,
        var restoreResult: RestoreResult = RestoreResult.Success
    ) : EntitlementRepository {
        val pro = MutableStateFlow(initialPro)
        override val isPro: Flow<Boolean> = pro
        override suspend fun refresh() {}
        override suspend fun purchase(activity: Activity): PurchaseResult {
            if (purchaseResult is PurchaseResult.Success) pro.value = true
            return purchaseResult
        }
        override suspend fun restore(): RestoreResult {
            if (restoreResult is RestoreResult.Success) pro.value = true
            return restoreResult
        }
    }

    /** Connectivity seam — true = online. */
    private class FakeConnectivity(online: Boolean) : Connectivity {
        val flow = MutableStateFlow(online)
        override val isOnline: Flow<Boolean> = flow
    }

    private fun vm(
        entitlement: FakeEntitlement = FakeEntitlement(),
        connectivity: FakeConnectivity = FakeConnectivity(online = true),
        offerings: List<String> = listOf("Memolio Pro")
    ): PaywallViewModel {
        entitlement.offeringTitles = offerings
        return PaywallViewModel(entitlement, connectivity) { offerings }
    }

    @Test
    fun offlineAndNotProShowsConnectToUnlock() = runTest {
        val vm = vm(connectivity = FakeConnectivity(online = false))
        vm.state.test {
            var s = awaitItem()
            while (!s.offline && !s.isPro) s = awaitItem()
            assertThat(s.offline).isTrue()
            assertThat(s.isPro).isFalse()
        }
    }

    @Test
    fun onlineLoadsOfferingTitles() = runTest {
        val vm = vm(offerings = listOf("Memolio Pro — one-time"))
        vm.state.test {
            var s = awaitItem()
            while (s.offerings.isEmpty() && !s.offline) s = awaitItem()
            assertThat(s.offerings).contains("Memolio Pro — one-time")
            assertThat(s.offline).isFalse()
        }
    }

    @Test
    fun successfulPurchaseFlipsIsProTrue() = runTest {
        val ent = FakeEntitlement(purchaseResult = PurchaseResult.Success)
        val vm = PaywallViewModel(ent, FakeConnectivity(true)) { listOf("Memolio Pro") }
        vm.purchase(activity)
        vm.state.test {
            var s = awaitItem()
            while (!s.isPro) s = awaitItem()
            assertThat(s.isPro).isTrue()
        }
    }

    @Test
    fun cancelledPurchaseLeavesNotProAndNoError() = runTest {
        val ent = FakeEntitlement(purchaseResult = PurchaseResult.Cancelled)
        val vm = PaywallViewModel(ent, FakeConnectivity(true)) { listOf("Memolio Pro") }
        vm.purchase(activity)
        vm.state.test {
            val s = awaitItem()
            assertThat(s.isPro).isFalse()
            assertThat(s.error).isNull()
        }
    }

    @Test
    fun erroredPurchaseSurfacesErrorMessage() = runTest {
        val ent = FakeEntitlement(purchaseResult = PurchaseResult.Error("network down"))
        val vm = PaywallViewModel(ent, FakeConnectivity(true)) { listOf("Memolio Pro") }
        vm.purchase(activity)
        vm.state.test {
            var s = awaitItem()
            while (s.error == null) s = awaitItem()
            assertThat(s.error).isEqualTo("network down")
            assertThat(s.isPro).isFalse()
        }
    }

    @Test
    fun successfulRestoreFlipsIsProTrue() = runTest {
        val ent = FakeEntitlement(restoreResult = RestoreResult.Success)
        val vm = PaywallViewModel(ent, FakeConnectivity(true)) { listOf("Memolio Pro") }
        vm.restore()
        vm.state.test {
            var s = awaitItem()
            while (!s.isPro) s = awaitItem()
            assertThat(s.isPro).isTrue()
        }
    }

    @Test
    fun erroredRestoreSurfacesError() = runTest {
        val ent = FakeEntitlement(restoreResult = RestoreResult.Error("nothing to restore"))
        val vm = PaywallViewModel(ent, FakeConnectivity(true)) { listOf("Memolio Pro") }
        vm.restore()
        vm.state.test {
            var s = awaitItem()
            while (s.error == null) s = awaitItem()
            assertThat(s.error).isEqualTo("nothing to restore")
        }
    }
}
