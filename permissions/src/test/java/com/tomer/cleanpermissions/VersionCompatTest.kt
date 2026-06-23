package com.tomer.cleanpermissions

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.tomer.cleanpermissions.internal.VersionCompat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies the per-Android-version behavior encoded in [VersionCompat]:
 * notification auto-grant below API 33, the normal/dangerous distinction, and
 * graceful handling of unknown permissions.
 */
@RunWith(RobolectricTestRunner::class)
class VersionCompatTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // API 31, below Tiramisu
    fun `POST_NOTIFICATIONS is auto-granted below API 33`() {
        assertTrue(VersionCompat.isAutoGranted(context, Manifest.permission.POST_NOTIFICATIONS))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun `POST_NOTIFICATIONS requires a runtime request on API 33+`() {
        assertFalse(VersionCompat.isAutoGranted(context, Manifest.permission.POST_NOTIFICATIONS))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `dangerous runtime permission is not auto-granted`() {
        assertFalse(VersionCompat.isAutoGranted(context, Manifest.permission.CAMERA))
    }

    @Test
    fun `normal install-time permission is auto-granted`() {
        // INTERNET is a normal-protection permission: no runtime prompt, so it
        // must be reported as granted rather than pushed through the flow.
        assertTrue(VersionCompat.isAutoGranted(context, Manifest.permission.INTERNET))
    }

    @Test
    fun `unknown permission is not auto-granted`() {
        // Edge case: an undeclared / non-existent permission string must not be
        // silently reported as granted.
        assertFalse(VersionCompat.isAutoGranted(context, "com.example.permission.DOES_NOT_EXIST"))
    }

    @Test
    fun `background location is recognised`() {
        assertTrue(VersionCompat.isBackgroundLocation(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        assertFalse(VersionCompat.isBackgroundLocation(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun `background location needs a separate request on API 30+`() {
        assertTrue(VersionCompat.backgroundLocationNeedsSeparateRequest())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun `background location does not need a separate request below API 30`() {
        assertFalse(VersionCompat.backgroundLocationNeedsSeparateRequest())
    }
}
