package com.tomer.grantkit.model

/**
 * The resolved state of a single Android runtime permission.
 *
 * Unlike the raw platform APIs, GrantKit distinguishes between a
 * permission that has *never been requested* and one that the user has
 * *permanently denied* ("don't ask again"). Those two situations are
 * indistinguishable through [android.app.Activity.shouldShowRequestPermissionRationale]
 * alone, and telling them apart is the core reason this library exists.
 *
 * @see isGranted for a convenient boolean check.
 */
public sealed interface PermissionStatus {

    /** The permission is currently granted and the protected API may be used. */
    public data object Granted : PermissionStatus

    /**
     * The permission has never been requested on this device/install.
     *
     * Show your first-time UI here (for example, a button that calls
     * [com.tomer.grantkit.model.PermissionState.request]).
     */
    public data object NotRequested : PermissionStatus

    /**
     * The permission was requested and denied, but the system will still show
     * the request dialog if asked again.
     *
     * This is the right moment to display a rationale explaining why the
     * permission is needed before calling
     * [com.tomer.grantkit.model.PermissionState.request] a second time.
     */
    public data object Denied : PermissionStatus

    /**
     * The user selected "don't ask again" (or the OS otherwise blocks further
     * prompts). The system dialog can no longer be shown, so the only way to
     * grant the permission is through the app's settings screen via
     * [com.tomer.grantkit.model.PermissionState.openSettings].
     */
    public data object PermanentlyDenied : PermissionStatus
}

/** Convenience accessor: `true` only when the status is [PermissionStatus.Granted]. */
public val PermissionStatus.isGranted: Boolean
    get() = this is PermissionStatus.Granted
