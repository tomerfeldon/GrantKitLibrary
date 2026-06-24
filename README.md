# GrantKit

A Compose-first Android runtime-permissions library that finally tells
**"never asked"** apart from **"don't ask again."**

The standard Android API and Google's Accompanist can't reliably distinguish
between a permission the user has *never been asked* for and one they've
*permanently denied* - both look identical through
`shouldShowRequestPermissionRationale()`. GrantKit solves exactly this,
behind one tiny declarative API: one public entry point, small config objects,
sensible defaults, all complexity hidden internally.

## Screenshots

<p align="center">
  <img src="https://github.com/tomerfeldon/GrantKitLibrary/blob/43c76d4eb6f39b3d34db18aa3c61a357e7242e08/Screenshot_20260624_184347.png" width="30%" /> 
  <img src="https://github.com/tomerfeldon/GrantKitLibrary/blob/4ad67437114cec2f783afbc7e60c7f7eadeaa8b1/Screenshot_20260624_184448.png" width="30%" />
  <img src="https://github.com/tomerfeldon/GrantKitLibrary/blob/4ad67437114cec2f783afbc7e60c7f7eadeaa8b1/Screenshot_20260624_184904.png" width="30%" />
</p>

> 📷 Screenshots of the included `:sample` app will be added here. Run
> `./gradlew :sample:installDebug` to see the full lifecycle on a device.

## Features

- 🎯 **One public entry point** - `rememberPermission` / `rememberMultiplePermissions`.
- 🔍 **Permanent-denial detection** - reliably separates `NotRequested` from
  `PermanentlyDenied`, the gap every other solution gets wrong.
- ⚡ **Compose-native** - `status` is backed by Compose state, so your UI
  recomposes automatically when a result comes back.
- 🔄 **Auto-refresh on resume** - re-checks status when the app returns to the
  foreground, in case the user changed it in system settings.
- 👥 **Multiple permissions** - request a group at once with aggregated state.
- 🤖 **Version-aware** - `POST_NOTIFICATIONS` auto-granted below API 33,
  background-location handling, normal/install-time permissions reported correctly.
- ⚙️ **Settings deep-link** - `openSettings()` for permanently-denied permissions.
- 💾 **Survives recreation** - state re-derived from the system as the source of truth.
- 📦 **Zero third-party dependencies** - only AndroidX Core and Jetpack Compose.
- 📝 **Fully documented public API** - small surface, stable naming, KDoc on everything.

## Installation

### JitPack

Add JitPack to your project-level `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your module-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.tomerfeldon.GrantKitLibrary:grantkit:1.0.0")
}
```

> Replace `1.0.0` with the latest released tag. The coordinate follows JitPack's
> multi-module format: `com.github.<user>.<repo>:<artifact>:<tag>`.

### Local Module

```kotlin
// settings.gradle.kts
include(":permissions")

// app/build.gradle.kts
dependencies {
    implementation(project(":permissions"))
}
```

## Quick Start

```kotlin
import android.Manifest
import com.tomer.grantkit.rememberPermission
import com.tomer.grantkit.model.PermissionStatus

@Composable
fun CameraScreen() {
    val camera = rememberPermission(Manifest.permission.CAMERA)
    when (camera.status) {
        PermissionStatus.Granted           -> CameraContent()
        PermissionStatus.PermanentlyDenied -> SettingsPrompt(onClick = camera::openSettings)
        else                               -> RequestPrompt(onClick = camera::request)
    }
}
```

That's it. No configuration needed for correct, default behavior.

## Usage Examples

### Single Permission (Camera)

```kotlin
val camera = rememberPermission(Manifest.permission.CAMERA)

Button(onClick = camera::request, enabled = !camera.status.isGranted) {
    Text(if (camera.status.isGranted) "Camera ready" else "Enable camera")
}
```

### Handling Permanent Denial

Once the user chooses "don't ask again", the system dialog can no longer be
shown - the only way forward is the app's settings screen.

```kotlin
val mic = rememberPermission(Manifest.permission.RECORD_AUDIO)

when (mic.status) {
    PermissionStatus.PermanentlyDenied ->
        Button(onClick = mic::openSettings) { Text("Open settings") }
    PermissionStatus.Denied ->
        Column {
            Text("We need the mic to record audio.") // rationale
            Button(onClick = mic::request) { Text("Allow") }
        }
    PermissionStatus.NotRequested ->
        Button(onClick = mic::request) { Text("Request") }
    PermissionStatus.Granted ->
        Text("Granted")
}
```

### Requesting Multiple Permissions

```kotlin
val perms = rememberMultiplePermissions(
    listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
    )
)

if (perms.allGranted) {
    StartRecording()
} else {
    Button(onClick = perms::requestAll) { Text("Grant all") }
}

// Inspect individual results:
perms.statuses.forEach { (permission, status) ->
    Text("$permission → $status")
}
```

### Notifications (Android 13+)

`POST_NOTIFICATIONS` only requires a runtime request on Android 13 (API 33) and
above. On older versions GrantKit reports it as `Granted` automatically -
no version checks in your code.

```kotlin
val notifications = rememberPermission(Manifest.permission.POST_NOTIFICATIONS)
// On API < 33 this is already PermissionStatus.Granted.
if (!notifications.status.isGranted) {
    Button(onClick = notifications::request) { Text("Enable notifications") }
}
```

### Background Location

On Android 11 (API 30) and above, `ACCESS_BACKGROUND_LOCATION` must be requested
**separately**, *after* foreground location has been granted. Request foreground
first, then background on its own:

```kotlin
val fine = rememberPermission(Manifest.permission.ACCESS_FINE_LOCATION)
val background = rememberPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

when {
    !fine.status.isGranted       -> Button(onClick = fine::request) { Text("Allow location") }
    !background.status.isGranted  -> Button(onClick = background::request) { Text("Allow in background") }
    else                          -> Text("Location ready")
}
```

## API Reference

### Public Composables

```kotlin
@Composable
fun rememberPermission(
    permission: String,
    config: GrantKitConfig = GrantKitConfig.Default,
): PermissionState

@Composable
fun rememberMultiplePermissions(
    permissions: List<String>,
    config: GrantKitConfig = GrantKitConfig.Default,
): MultiplePermissionsState
```

### Data Models

`PermissionState` - observable state of a single permission:

| Member | Description |
| --- | --- |
| `permission: String` | The Android permission string this state represents. |
| `status: PermissionStatus` | Current, auto-updating status. |
| `request()` | Launches the system permission dialog (no-op if already granted). |
| `openSettings()` | Opens this app's settings screen. |

`MultiplePermissionsState` - observable state of a group:

| Member | Description |
| --- | --- |
| `permissions: List<PermissionState>` | Per-permission state, in input order. |
| `allGranted: Boolean` | `true` when every permission is granted. |
| `statuses: Map<String, PermissionStatus>` | Each permission mapped to its status. |
| `requestAll()` | Requests every not-yet-granted permission in one dialog. |
| `openSettings()` | Opens this app's settings screen. |

### Configuration

```kotlin
val config = GrantKitConfig(
    refreshOnResume = true, // re-check status on ON_RESUME (default)
)
```

| Property | Default | Description |
| --- | --- | --- |
| `refreshOnResume` | `true` | Re-check each permission against the system when the app returns to the foreground, keeping the UI in sync with changes made in system settings. |

### PermissionStatus

A sealed interface with four states:

| Status | Meaning |
| --- | --- |
| `Granted` | Permission is granted; the protected API may be used. |
| `NotRequested` | Never requested on this install - show your first-time UI. |
| `Denied` | Denied once, can ask again - a good time to show a rationale. |
| `PermanentlyDenied` | "Don't ask again" - only changeable via `openSettings()`. |

```kotlin
val PermissionStatus.isGranted: Boolean // true only for Granted
```

## Project Structure

```
permissions/src/main/java/com/tomer/grantkit/
├── GrantKit.kt            # Public API: rememberPermission, rememberMultiplePermissions
├── model/
│   ├── PermissionStatus.kt        # Granted / NotRequested / Denied / PermanentlyDenied
│   ├── PermissionState.kt         # status + request() + openSettings()
│   └── MultiplePermissionsState.kt
├── config/
│   └── GrantKitConfig.kt  # small, optional behavior config
└── internal/
    ├── PermissionChecker.kt       # checkSelfPermission + shouldShowRationale + resolution
    ├── PermissionRequester.kt     # bridge to the Activity Result API launcher
    ├── DeniedTracker.kt           # "have we asked before?" memory (SharedPreferences)
    ├── SettingsLauncher.kt        # opens the app's system settings screen
    └── VersionCompat.kt           # per-Android-version differences
```

## Edge Cases Handled

- **Undeclared permission** - a permission not in your `AndroidManifest.xml` is
  handled gracefully instead of crashing.
- **Rapid repeated `request()`** - the "requested" flag is idempotent and the
  Activity Result launcher de-dupes in-flight requests.
- **Grant in settings, then return** - status refreshes on `ON_RESUME`.
- **Configuration change / process recreation** - the requested flag persists in
  `SharedPreferences`, and status is re-derived from the system as source of truth.
- **Auto-granted permissions** - permissions that require no runtime request on
  the current Android version are reported as `Granted` directly.

## Requirements

- `minSdk = 24`, `compileSdk = 35`
- Kotlin + Jetpack Compose
- Dependencies: AndroidX Core and Jetpack Compose only - no Accompanist, no
  DataStore, no third-party libraries.

## License

```
MIT License

Copyright (c) 2026 Tomer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

See [LICENSE](LICENSE) for the full text.
