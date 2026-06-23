package com.tomer.grantkit.internal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build

/**
 * Centralizes the per-Android-version quirks of runtime permissions so the rest
 * of the library can treat every permission uniformly.
 *
 * Two rules are encoded here:
 *
 * - **[Manifest.permission.POST_NOTIFICATIONS]** is a runtime permission only on
 *   Android 13 (API 33) and above. On older versions it is implicitly granted,
 *   so [isAutoGranted] reports it as such.
 * - **Non-runtime permissions** (normal/install-time, e.g. `INTERNET`) never
 *   need a runtime request. They are reported as auto-granted rather than being
 *   pushed through the request flow, which avoids spurious "denied" states.
 *
 * Background location ([Manifest.permission.ACCESS_BACKGROUND_LOCATION]) is
 * documented via [isBackgroundLocation] / [BACKGROUND_LOCATION_SEPARATE_FROM_API];
 * see those members for the separate-request requirement on API 30+.
 */
internal object VersionCompat {

    /**
     * Android 11 (API 30) and above require background location to be requested
     * **separately**, in its own dialog, *after* foreground location has been
     * granted. Bundling it with a foreground request causes the OS to silently
     * drop the background grant. Callers should request foreground location
     * first, then request [Manifest.permission.ACCESS_BACKGROUND_LOCATION] on
     * its own once that succeeds.
     */
    const val BACKGROUND_LOCATION_SEPARATE_FROM_API: Int = Build.VERSION_CODES.R

    /** `true` if [permission] is [Manifest.permission.ACCESS_BACKGROUND_LOCATION]. */
    fun isBackgroundLocation(permission: String): Boolean =
        permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION

    /** `true` if a separate background-location request is required on this device. */
    fun backgroundLocationNeedsSeparateRequest(): Boolean =
        Build.VERSION.SDK_INT >= BACKGROUND_LOCATION_SEPARATE_FROM_API

    /**
     * `true` when [permission] requires no runtime request on the current device
     * and should therefore be reported as [com.tomer.grantkit.model.PermissionStatus.Granted]
     * without consulting the request flow.
     *
     * Covers two cases:
     * 1. Version-gated runtime permissions that don't exist yet on this OS
     *    (currently [Manifest.permission.POST_NOTIFICATIONS] below API 33).
     * 2. Permissions whose protection level is not "dangerous" — normal and
     *    install-time permissions are granted at install and never prompt.
     */
    fun isAutoGranted(context: Context, permission: String): Boolean {
        // Case 1: version-gated. Checked before any PackageManager lookup,
        // because these permissions may not exist on older systems at all.
        if (permission == Manifest.permission.POST_NOTIFICATIONS) {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        }
        // Case 2: not a dangerous runtime permission -> no prompt needed.
        return !isDangerous(context, permission)
    }

    /**
     * Whether [permission] is a "dangerous" (runtime) permission on this device.
     *
     * Unknown/undeclared permission strings (a documented edge case) resolve to
     * `true` here so they flow through the normal request path and surface their
     * real state instead of being silently treated as granted.
     */
    private fun isDangerous(context: Context, permission: String): Boolean {
        return try {
            val info = context.packageManager.getPermissionInfo(permission, 0)
            val protection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.protection
            } else {
                @Suppress("DEPRECATION")
                info.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE
            }
            protection == PermissionInfo.PROTECTION_DANGEROUS
        } catch (_: PackageManager.NameNotFoundException) {
            // Unknown permission: don't claim it's auto-granted.
            true
        }
    }
}
