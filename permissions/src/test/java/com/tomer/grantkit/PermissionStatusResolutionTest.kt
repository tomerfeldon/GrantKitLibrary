package com.tomer.grantkit

import com.tomer.grantkit.internal.PermissionChecker
import com.tomer.grantkit.model.PermissionStatus
import com.tomer.grantkit.model.isGranted
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure unit tests for [PermissionChecker.resolveStatus] — the core
 * status-resolution logic and the single reason this library exists. No Android
 * framework is involved, so these run as fast plain JVM tests.
 */
class PermissionStatusResolutionTest {

    @Test
    fun `granted wins regardless of other signals`() {
        // Every combination of the other two flags must still resolve to Granted.
        for (requested in listOf(false, true)) {
            for (rationale in listOf(false, true)) {
                val status = PermissionChecker.resolveStatus(
                    isGranted = true,
                    hasBeenRequested = requested,
                    shouldShowRationale = rationale,
                )
                assertEquals(PermissionStatus.Granted, status)
            }
        }
    }

    @Test
    fun `never requested resolves to NotRequested`() {
        val status = PermissionChecker.resolveStatus(
            isGranted = false,
            hasBeenRequested = false,
            shouldShowRationale = false,
        )
        assertEquals(PermissionStatus.NotRequested, status)
    }

    @Test
    fun `denied once with rationale resolves to Denied`() {
        val status = PermissionChecker.resolveStatus(
            isGranted = false,
            hasBeenRequested = true,
            shouldShowRationale = true,
        )
        assertEquals(PermissionStatus.Denied, status)
    }

    @Test
    fun `requested but no rationale resolves to PermanentlyDenied`() {
        val status = PermissionChecker.resolveStatus(
            isGranted = false,
            hasBeenRequested = true,
            shouldShowRationale = false,
        )
        assertEquals(PermissionStatus.PermanentlyDenied, status)
    }

    /**
     * The crux of the library: NotRequested and PermanentlyDenied are
     * indistinguishable through the platform alone (both have
     * `shouldShowRationale == false`). Only the persisted `hasBeenRequested`
     * flag separates them.
     */
    @Test
    fun `NotRequested and PermanentlyDenied differ only by the requested flag`() {
        val common = { requested: Boolean ->
            PermissionChecker.resolveStatus(
                isGranted = false,
                hasBeenRequested = requested,
                shouldShowRationale = false,
            )
        }
        assertEquals(PermissionStatus.NotRequested, common(false))
        assertEquals(PermissionStatus.PermanentlyDenied, common(true))
    }

    @Test
    fun `isGranted extension is true only for Granted`() {
        assertTrue(PermissionStatus.Granted.isGranted)
        assertFalse(PermissionStatus.NotRequested.isGranted)
        assertFalse(PermissionStatus.Denied.isGranted)
        assertFalse(PermissionStatus.PermanentlyDenied.isGranted)
    }
}
