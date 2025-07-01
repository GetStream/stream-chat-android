/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.sample.ui.location

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

@Composable
internal fun LocationPicker(
    viewModelFactory: SharedLocationViewModelFactory,
    onDismiss: () -> Unit = {},
) {
    val viewModel = viewModel(SharedLocationViewModel::class, factory = viewModelFactory)
    LocationPickerContent(
        onSendStaticLocation = viewModel::sendStaticLocation,
        onStartLiveLocationSharing = viewModel::startLiveLocationSharing,
        onDismiss = onDismiss,
    )
}

@Composable
private fun LocationPickerContent(
    onSendStaticLocation: (latitude: Double, longitude: Double) -> Unit = { _, _ -> },
    onStartLiveLocationSharing: (latitude: Double, longitude: Double, endAt: Date) -> Unit = { _, _, _ -> },
    onDismiss: () -> Unit = {},
) {
    var location by remember { mutableStateOf<Location?>(null) }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LocationContent(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { foundLocation ->
            location = foundLocation
        }
        LocationButton(
            icon = Icons.Rounded.LocationOn,
            label = "Share Live Location",
            description = "Share your location in real-time",
            isPrimary = true,
            onClick = {
                onStartLiveLocationSharing(
                    location?.latitude ?: 0.0,
                    location?.longitude ?: 0.0,
                    Calendar.getInstance().apply { add(Calendar.MINUTE, 15) }.time, // Share for 15 minutes
                )
                onDismiss()
            },
        )
        LocationButton(
            icon = Icons.AutoMirrored.Rounded.Send,
            label = "Send Current Location",
            description = "Share your current location",
            onClick = {
                onSendStaticLocation(
                    location?.latitude ?: 0.0,
                    location?.longitude ?: 0.0,
                )
                onDismiss()
            },
        )
    }
}

@Composable
private fun LocationContent(
    modifier: Modifier = Modifier,
    onLocationFound: (Location) -> Unit,
) {
    val context = LocalContext.current
    var location by remember { mutableStateOf<Location?>(null) }

    var locationPermissionTrigger by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                locationPermissionTrigger++
            }
        },
    )

    LaunchedEffect(locationPermissionTrigger) {
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val hasPermission = ContextCompat
            .checkSelfPermission(context, locationPermission) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(locationPermission)
        } else {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)

            val lastLocation = fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .await()

            location = lastLocation
            onLocationFound(lastLocation)
        }
    }

    if (location != null) {
        MapWebView(
            modifier = modifier,
            latitude = location!!.latitude,
            longitude = location!!.longitude,
        )
    } else {
        LoadingIndicator(modifier = modifier)
    }
}

@Composable
private fun LocationButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    description: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        shape = RoundedCornerShape(7.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isPrimary) ChatTheme.colors.primaryAccent else ChatTheme.colors.borders,
        ),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ChatTheme.colors.textHighEmphasis),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = label, style = ChatTheme.typography.title3Bold)
                Text(text = description, style = ChatTheme.typography.footnote)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationPickerContentPreview() {
    ChatTheme {
        LocationPickerContent()
    }
}
