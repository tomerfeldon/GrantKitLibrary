package com.tomer.grantkit.config

/**
 * Optional behavior configuration for GrantKit.
 *
 * Because this is a behavior library (not a visual one), the surface here is
 * intentionally tiny. The defaults are chosen to be correct for the vast
 * majority of apps; most consumers never need to construct this type directly.
 *
 * @property refreshOnResume When `true` (the default), every permission's
 *   status is re-checked against the system each time the host returns to the
 *   foreground (`Lifecycle.Event.ON_RESUME`). This keeps the UI in sync when
 *   the user changes a permission from system settings while the app is
 *   backgrounded. Set to `false` to opt out of that automatic refresh.
 */
public data class GrantKitConfig(
    val refreshOnResume: Boolean = true,
) {
    public companion object {
        /** The default configuration: refresh on resume enabled. */
        public val Default: GrantKitConfig = GrantKitConfig()
    }
}
