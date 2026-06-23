package com.tomer.cleanpermissions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tomer.cleanpermissions.config.CleanPermissionsConfig
import com.tomer.cleanpermissions.internal.DeniedTracker
import com.tomer.cleanpermissions.internal.PermissionChecker
import com.tomer.cleanpermissions.internal.PermissionRequester
import com.tomer.cleanpermissions.internal.SettingsLauncher
import com.tomer.cleanpermissions.internal.VersionCompat
import com.tomer.cleanpermissions.model.MultiplePermissionsState
import com.tomer.cleanpermissions.model.PermissionState
import com.tomer.cleanpermissions.model.PermissionStatus

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
    val context = LocalContext.current
    val checker = remember(context) { PermissionChecker(context) }
    val tracker = remember(context) { DeniedTracker(context) }
    val settingsLauncher = remember(context) { SettingsLauncher(context) }

    val statusState = remember(permission) {
        mutableStateOf(computeStatus(context, checker, tracker, permission))
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        // Re-derive from the system as the source of truth rather than trusting
        // the boolean result, so auto-grant and version quirks stay consistent.
        statusState.value = computeStatus(context, checker, tracker, permission)
    }

    val requester = remember(launcher, tracker) {
        PermissionRequester(tracker, launchSingle = { launcher.launch(it) })
    }

    RefreshOnResume(enabled = config.refreshOnResume, key = permission) {
        statusState.value = computeStatus(context, checker, tracker, permission)
    }

    return remember(permission, requester, settingsLauncher) {
        object : PermissionState {
            override val permission: String = permission
            override val status: PermissionStatus get() = statusState.value

            override fun request() {
                if (statusState.value !is PermissionStatus.Granted) {
                    requester.request(permission)
                }
            }

            override fun openSettings() = settingsLauncher.open()
        }
    }
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
    val context = LocalContext.current
    val checker = remember(context) { PermissionChecker(context) }
    val tracker = remember(context) { DeniedTracker(context) }
    val settingsLauncher = remember(context) { SettingsLauncher(context) }

    // A snapshot-observable map keyed by permission. Reading an entry inside a
    // composable subscribes it to that permission's changes.
    val statuses = remember(permissions) {
        mutableStateMapOf<String, PermissionStatus>().apply {
            permissions.forEach { put(it, computeStatus(context, checker, tracker, it)) }
        }
    }

    fun refreshAll() {
        permissions.forEach { statuses[it] = computeStatus(context, checker, tracker, it) }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        refreshAll()
    }

    val requester = remember(launcher, tracker) {
        PermissionRequester(
            tracker,
            launchSingle = { launcher.launch(arrayOf(it)) },
            launchMultiple = { launcher.launch(it) },
        )
    }

    RefreshOnResume(enabled = config.refreshOnResume, key = permissions) { refreshAll() }

    val children = remember(permissions, requester, settingsLauncher) {
        permissions.map { perm ->
            object : PermissionState {
                override val permission: String = perm
                override val status: PermissionStatus
                    get() = statuses[perm] ?: PermissionStatus.NotRequested

                override fun request() {
                    if (statuses[perm] !is PermissionStatus.Granted) requester.request(perm)
                }

                override fun openSettings() = settingsLauncher.open()
            }
        }
    }

    return remember(children, requester, settingsLauncher) {
        object : MultiplePermissionsState {
            override val permissions: List<PermissionState> = children
            override val allGranted: Boolean
                get() = children.all { it.status is PermissionStatus.Granted }
            override val statuses: Map<String, PermissionStatus>
                get() = children.associate { it.permission to it.status }

            override fun requestAll() {
                val pending = children
                    .filter { it.status !is PermissionStatus.Granted }
                    .map { it.permission }
                requester.requestAll(pending)
            }

            override fun openSettings() = settingsLauncher.open()
        }
    }
}

/**
 * Resolves the canonical [PermissionStatus] for [permission], folding in the
 * version-specific auto-grant rules before applying the core four-step logic.
 */
private fun computeStatus(
    context: Context,
    checker: PermissionChecker,
    tracker: DeniedTracker,
    permission: String,
): PermissionStatus {
    if (VersionCompat.isAutoGranted(context, permission)) return PermissionStatus.Granted

    val activity = context.findActivity()
    return PermissionChecker.resolveStatus(
        isGranted = checker.isGranted(permission),
        hasBeenRequested = tracker.hasBeenRequested(permission),
        // Without an Activity we can't read the rationale flag; default to false,
        // which keeps a requested-but-undeterminable permission on the safe side.
        shouldShowRationale = activity?.let { checker.shouldShowRationale(it, permission) } ?: false,
    )
}

/**
 * Registers an `ON_RESUME` observer that re-checks permission status whenever
 * the host returns to the foreground (the user may have toggled a permission in
 * system settings). No-op when [enabled] is false.
 */
@Composable
private fun RefreshOnResume(enabled: Boolean, key: Any?, onResume: () -> Unit) {
    if (!enabled) return
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, key) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

/** Walks the [ContextWrapper] chain to find the host [Activity], if any. */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
