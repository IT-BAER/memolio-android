package com.baer.memolio.appliance

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ManifestApplianceTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    // Resolve at runtime: debug builds carry a .debug applicationId suffix, but the
    // component class names (receiver/service) keep the unsuffixed namespace.
    private val pkg get() = context.packageName

    @Test
    fun declaresBootAndForegroundServicePermissions() {
        val info = context.packageManager.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS)
        val requested = info.requestedPermissions?.toSet().orEmpty()
        assertThat(requested).containsAtLeast(
            "android.permission.RECEIVE_BOOT_COMPLETED",
            "android.permission.FOREGROUND_SERVICE",
            "android.permission.FOREGROUND_SERVICE_SPECIAL_USE",
            "android.permission.WAKE_LOCK",
            "android.permission.POST_NOTIFICATIONS"
        )
    }

    @Test
    fun bootReceiverIsRegistered() {
        val info = context.packageManager.getPackageInfo(pkg, PackageManager.GET_RECEIVERS)
        val names = info.receivers?.map { it.name }.orEmpty()
        assertThat(names).contains("com.baer.memolio.appliance.BootReceiver")
    }

    @Test
    fun frameServiceDeclaresSpecialUseForegroundType() {
        val info = context.packageManager.getPackageInfo(pkg, PackageManager.GET_SERVICES)
        val frame = info.services?.firstOrNull { it.name == "com.baer.memolio.service.FrameService" }
        assertThat(frame).isNotNull()
        val specialUse = 1 shl 30
        assertThat(frame!!.foregroundServiceType and specialUse).isEqualTo(specialUse)
    }
}
