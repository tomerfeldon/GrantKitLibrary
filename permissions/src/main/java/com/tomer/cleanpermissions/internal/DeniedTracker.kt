package com.tomer.cleanpermissions.internal

import android.content.Context

/**
 * Remembers whether a permission request has *ever* been launched for a given
 * permission on this install.
 *
 * This single bit of persisted state is what lets CleanPermissions tell apart
 * "never asked" from "permanently denied" — two situations the platform APIs
 * report identically. We persist it in [android.content.SharedPreferences]
 * (deliberately no DataStore dependency) keyed by the permission string.
 *
 * The flag is set at the moment a request is *launched* (see
 * [PermissionRequester]), never before.
 */
internal class DeniedTracker(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** `true` once a request has ever been launched for [permission]. */
    fun hasBeenRequested(permission: String): Boolean =
        prefs.getBoolean(permission, false)

    /** Records that a request has been launched for [permission]. Idempotent. */
    fun markRequested(permission: String) {
        if (!hasBeenRequested(permission)) {
            prefs.edit().putBoolean(permission, true).apply()
        }
    }

    private companion object {
        const val PREFS_NAME = "com.tomer.cleanpermissions.tracker"
    }
}
