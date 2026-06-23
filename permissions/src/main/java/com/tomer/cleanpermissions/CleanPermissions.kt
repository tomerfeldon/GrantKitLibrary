package com.tomer.cleanpermissions

import androidx.compose.runtime.Composable
import com.tomer.cleanpermissions.config.CleanPermissionsConfig
import com.tomer.cleanpermissions.model.MultiplePermissionsState
import com.tomer.cleanpermissions.model.PermissionState

/**
 * Remembers and observes the state of a single Android runtime [permission].
 *
 * The returned [PermissionState] exposes a Compose-backed
 * [PermissionState.status] that recomposes your UI automatically when the
 * permission result arrives or when the app returns to the foreground. Call
 * [PermissionState.request] to show the system dialog and
 * [PermissionState.openSettings] to deep-link into app settings.
 *
 * ```
 * val camera = rememberPermission(Manifest.permission.CAMERA)
 * if (camera.status.isGranted) CameraContent() else RequestPrompt(camera::request)
 * ```
 *
 * @param permission an Android permission string, e.g. `Manifest.permission.CAMERA`.
 * @param config optional [CleanPermissionsConfig] tuning runtime behavior.
 */
@Composable
public fun rememberPermission(
    permission: String,
    config: CleanPermissionsConfig = CleanPermissionsConfig.Default,
): PermissionState {
    // TODO(phase 3+): wire up PermissionChecker, PermissionRequester,
    //  DeniedTracker, SettingsLauncher and the Activity Result API launcher.
    TODO("Internal implementation lands in a later phase")
}

/**
 * Remembers and observes the state of a group of runtime [permissions]
 * requested together.
 *
 * @param permissions the Android permission strings to track.
 * @param config optional [CleanPermissionsConfig] tuning runtime behavior.
 * @see rememberPermission
 */
@Composable
public fun rememberMultiplePermissions(
    permissions: List<String>,
    config: CleanPermissionsConfig = CleanPermissionsConfig.Default,
): MultiplePermissionsState {
    // TODO(phase 3+): wire up the RequestMultiplePermissions contract and
    //  aggregate per-permission state.
    TODO("Internal implementation lands in a later phase")
}
