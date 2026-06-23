package com.tomer.cleanpermissions.sample

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tomer.cleanpermissions.model.PermissionState
import com.tomer.cleanpermissions.model.PermissionStatus
import com.tomer.cleanpermissions.rememberPermission

/**
 * Single-screen demo exercising CleanPermissions end to end: Camera,
 * Notifications and Fine Location, each with a live [PermissionStatus] and the
 * appropriate action (request / open settings).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SampleScreen()
                }
            }
        }
    }
}

@Composable
private fun SampleScreen() {
    val camera = rememberPermission(Manifest.permission.CAMERA)
    val notifications = rememberPermission(Manifest.permission.POST_NOTIFICATIONS)
    val location = rememberPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "CleanPermissions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Tap a permission to see the full lifecycle, including the " +
                "\"don't ask again\" state that the standard APIs can't detect.",
            style = MaterialTheme.typography.bodyMedium,
        )

        PermissionCard(
            title = "Camera",
            rationale = "We need the camera to take photos.",
            state = camera,
        )
        PermissionCard(
            title = "Notifications",
            rationale = "Notifications keep you up to date.",
            state = notifications,
        )
        PermissionCard(
            title = "Fine Location",
            rationale = "Location is used to show nearby results.",
            state = location,
        )
    }
}

@Composable
private fun PermissionCard(
    title: String,
    rationale: String,
    state: PermissionState,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)

            Text(
                text = "Status: ${state.status.label}",
                style = MaterialTheme.typography.bodyMedium,
                color = state.status.color,
                fontWeight = FontWeight.Medium,
            )

            when (state.status) {
                PermissionStatus.Granted -> {
                    // Nothing to do — show the protected feature in a real app.
                }

                PermissionStatus.PermanentlyDenied -> {
                    Text(
                        text = "Permission was permanently denied. Enable it from settings.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(onClick = state::openSettings) {
                        Text("Open settings")
                    }
                }

                PermissionStatus.Denied -> {
                    Text(text = rationale, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = state::request) {
                        Text("Request again")
                    }
                }

                PermissionStatus.NotRequested -> {
                    Button(onClick = state::request) {
                        Text("Request")
                    }
                }
            }
        }
    }
}

private val PermissionStatus.label: String
    get() = when (this) {
        PermissionStatus.Granted -> "Granted"
        PermissionStatus.NotRequested -> "Not requested"
        PermissionStatus.Denied -> "Denied (can ask again)"
        PermissionStatus.PermanentlyDenied -> "Permanently denied"
    }

private val PermissionStatus.color: Color
    get() = when (this) {
        PermissionStatus.Granted -> Color(0xFF2E7D32)
        PermissionStatus.Denied -> Color(0xFFEF6C00)
        PermissionStatus.PermanentlyDenied -> Color(0xFFC62828)
        PermissionStatus.NotRequested -> Color.Unspecified
    }
