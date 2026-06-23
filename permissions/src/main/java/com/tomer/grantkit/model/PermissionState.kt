package com.tomer.grantkit.model

import androidx.compose.runtime.Stable

/**
 * Observable state of a single runtime permission.
 *
 * Obtain an instance with
 * [com.tomer.grantkit.rememberPermission]. The [status] property is
 * backed by Compose snapshot state, so reading it inside a composable will
 * automatically recompose your UI when the permission result arrives or when
 * the app returns to the foreground (the user may have changed the permission
 * from system settings while the app was backgrounded).
 *
 * Typical usage:
 * ```
 * val camera = rememberPermission(Manifest.permission.CAMERA)
 * when (camera.status) {
 *     PermissionStatus.Granted           -> CameraContent()
 *     PermissionStatus.PermanentlyDenied -> SettingsPrompt(onClick = camera::openSettings)
 *     else                               -> RequestPrompt(onClick = camera::request)
 * }
 * ```
 */
@Stable
public interface PermissionState {

    /** The Android permission string this state represents, e.g. `Manifest.permission.CAMERA`. */
    public val permission: String

    /** The current, automatically-updating [PermissionStatus] of [permission]. */
    public val status: PermissionStatus

    /**
     * Launches the system permission dialog.
     *
     * Has no effect when the permission is already granted. When the status is
     * [PermissionStatus.PermanentlyDenied] the OS will not show a dialog; use
     * [openSettings] instead.
     */
    public fun request()

    /** Opens this app's settings screen so the user can change the permission manually. */
    public fun openSettings()
}
