package com.tomer.cleanpermissions.internal

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Opens this app's entry in the system Settings app, where the user can change
 * a permission that was permanently denied ("don't ask again").
 *
 * This is the only path back to granting a permission once the system stops
 * showing the request dialog, so it's surfaced via
 * [com.tomer.cleanpermissions.model.PermissionState.openSettings].
 */
internal class SettingsLauncher(private val context: Context) {

    /**
     * Launches the app-details settings screen for this package.
     *
     * Adds [Intent.FLAG_ACTIVITY_NEW_TASK] so it can be started from a
     * non-Activity [Context] (for example an application context).
     */
    fun open() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
