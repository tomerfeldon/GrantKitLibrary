package com.tomer.cleanpermissions

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tomer.cleanpermissions.internal.DeniedTracker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Verifies the persistence behavior of [DeniedTracker], the "have we ever
 * launched a request?" memory backing PermanentlyDenied detection.
 */
@RunWith(RobolectricTestRunner::class)
class DeniedTrackerTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val permission = "android.permission.CAMERA"

    @Test
    fun `unmarked permission has not been requested`() {
        val tracker = DeniedTracker(context)
        assertFalse(tracker.hasBeenRequested(permission))
    }

    @Test
    fun `marking records the permission as requested`() {
        val tracker = DeniedTracker(context)
        tracker.markRequested(permission)
        assertTrue(tracker.hasBeenRequested(permission))
    }

    @Test
    fun `marking is idempotent`() {
        val tracker = DeniedTracker(context)
        tracker.markRequested(permission)
        tracker.markRequested(permission)
        assertTrue(tracker.hasBeenRequested(permission))
    }

    @Test
    fun `flag is tracked per permission`() {
        val tracker = DeniedTracker(context)
        tracker.markRequested(permission)
        assertTrue(tracker.hasBeenRequested(permission))
        assertFalse(tracker.hasBeenRequested("android.permission.ACCESS_FINE_LOCATION"))
    }

    /**
     * Edge case: configuration change / process recreation. A fresh tracker
     * instance (simulating a new process) must still see the persisted flag,
     * because the system + SharedPreferences are the source of truth.
     */
    @Test
    fun `flag survives a new tracker instance`() {
        DeniedTracker(context).markRequested(permission)

        val recreated = DeniedTracker(context)
        assertTrue(recreated.hasBeenRequested(permission))
    }
}
