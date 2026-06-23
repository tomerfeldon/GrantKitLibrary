package com.tomer.cleanpermissions.internal

/**
 * Bridges a [com.tomer.cleanpermissions.model.PermissionState.request] call to
 * an Activity Result API launcher, while guaranteeing the
 * [DeniedTracker.markRequested] flag is set *at the moment a request is
 * launched* — the precise instant required for correct PermanentlyDenied
 * detection later.
 *
 * The actual launcher is owned by the composable (created via
 * `rememberLauncherForActivityResult`); it is injected here as a plain lambda
 * so this class stays free of Compose/Android plumbing and easy to reason about.
 *
 * @param tracker the persisted "have we asked?" memory.
 * @param launchSingle invokes a `RequestPermission` launcher with one permission.
 * @param launchMultiple invokes a `RequestMultiplePermissions` launcher.
 */
internal class PermissionRequester(
    private val tracker: DeniedTracker,
    private val launchSingle: (String) -> Unit,
    private val launchMultiple: (Array<String>) -> Unit,
) {

    /**
     * Launches a request for a single [permission]. Marks it as requested
     * first so that, regardless of the user's choice, status resolution can
     * distinguish a denial from a never-asked permission.
     */
    fun request(permission: String) {
        tracker.markRequested(permission)
        launchSingle(permission)
    }

    /**
     * Launches a single grouped request for all [permissions]. Every entry is
     * marked as requested before the system dialog is shown.
     */
    fun requestAll(permissions: List<String>) {
        if (permissions.isEmpty()) return
        permissions.forEach(tracker::markRequested)
        launchMultiple(permissions.toTypedArray())
    }
}
