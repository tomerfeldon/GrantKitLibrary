package com.tomer.cleanpermissions.model

import androidx.compose.runtime.Stable

/**
 * Observable state for a group of runtime permissions requested together.
 *
 * Obtain an instance with
 * [com.tomer.cleanpermissions.rememberMultiplePermissions]. Like
 * [PermissionState], every property is backed by Compose snapshot state and
 * refreshes automatically on result delivery and on app resume.
 */
@Stable
public interface MultiplePermissionsState {

    /** The individual [PermissionState] for each requested permission, in input order. */
    public val permissions: List<PermissionState>

    /** `true` when every permission in the group is currently granted. */
    public val allGranted: Boolean

    /** A snapshot of each permission string mapped to its current [PermissionStatus]. */
    public val statuses: Map<String, PermissionStatus>

    /**
     * Launches a single system dialog requesting every not-yet-granted
     * permission in the group at once.
     */
    public fun requestAll()

    /** Opens this app's settings screen so the user can change the permissions manually. */
    public fun openSettings()
}
