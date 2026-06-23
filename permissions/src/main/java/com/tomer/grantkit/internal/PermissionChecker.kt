package com.tomer.grantkit.internal

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tomer.grantkit.model.PermissionStatus

/**
 * Thin wrappers over the two platform APIs that report permission state, plus
 * the pure logic that resolves them — together with the [DeniedTracker] flag —
 * into a single unambiguous [PermissionStatus].
 *
 * Keeping [resolveStatus] pure (no Android types) is what makes the core
 * differentiator straightforward to unit test.
 */
internal class PermissionChecker(private val context: Context) {

    /** `true` when [permission] is currently granted. */
    fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Wraps [ActivityCompat.shouldShowRequestPermissionRationale].
     *
     * Note the platform ambiguity this method carries: it returns `false` both
     * when the permission was never requested *and* when the user chose
     * "don't ask again". [resolveStatus] disambiguates using the persisted
     * `hasBeenRequested` flag.
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    internal companion object {

        /**
         * Resolves the canonical [PermissionStatus] from the three primitive
         * signals. Pure and deterministic, mirroring the documented contract:
         *
         * 1. granted                      -> [PermissionStatus.Granted]
         * 2. never requested before       -> [PermissionStatus.NotRequested]
         * 3. rationale should be shown     -> [PermissionStatus.Denied] (can ask again)
         * 4. otherwise                     -> [PermissionStatus.PermanentlyDenied]
         *
         * @param isGranted result of [isGranted].
         * @param hasBeenRequested the persisted [DeniedTracker] flag.
         * @param shouldShowRationale result of [shouldShowRationale].
         */
        fun resolveStatus(
            isGranted: Boolean,
            hasBeenRequested: Boolean,
            shouldShowRationale: Boolean,
        ): PermissionStatus = when {
            isGranted -> PermissionStatus.Granted
            !hasBeenRequested -> PermissionStatus.NotRequested
            shouldShowRationale -> PermissionStatus.Denied
            else -> PermissionStatus.PermanentlyDenied
        }
    }
}
