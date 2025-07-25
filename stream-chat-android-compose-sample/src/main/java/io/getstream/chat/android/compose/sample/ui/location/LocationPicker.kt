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
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.sample.ui.component.MapWebView
import io.getstream.chat.android.compose.sample.vm.SharedLocationViewModel
import io.getstream.chat.android.compose.sample.vm.SharedLocationViewModelFactory
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.uiutils.util.openSystemSettings
import kotlinx.coroutines.tasks.await
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
    var showDurationDropdownMenu by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LocationContent(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onLocationFound = { foundLocation ->
                location = foundLocation
            },
            onDismiss = onDismiss,
        )

        Box {
            LocationButton(
                enabled = location != null,
                icon = Icons.Rounded.NearMe,
                label = "Share Live Location",
                description = "Share your location in real-time",
                isPrimary = true,
                onClick = { showDurationDropdownMenu = true },
            )
            DurationDropdownMenu(
                expanded = showDurationDropdownMenu,
                onSelect = { duration ->
                    showDurationDropdownMenu = false
                    onStartLiveLocationSharing(
                        location!!.latitude,
                        location!!.longitude,
                        duration.asDate(),
                    )
                    onDismiss()
                },
                onDismiss = { showDurationDropdownMenu = false },
            )
        }
        LocationButton(
            enabled = location != null,
            icon = Icons.Rounded.LocationOn,
            label = "Send Current Location",
            description = "Share your current location",
            onClick = {
                onSendStaticLocation(
                    location!!.latitude,
                    location!!.longitude,
                )
                onDismiss()
            },
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun LocationContent(
    modifier: Modifier = Modifier,
    onLocationFound: (Location) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var location by remember { mutableStateOf<Location?>(null) }

    var locationPermissionTrigger by remember { mutableIntStateOf(0) }
    var showLocationPermissionRequired by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            locationPermissionTrigger++
        } else {
            showLocationPermissionRequired = true
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            locationPermissionTrigger++
        } else {
            onDismiss()
        }
    }

    LaunchedEffect(locationPermissionTrigger) {
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val hasPermission = ContextCompat
            .checkSelfPermission(context, locationPermission) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(locationPermission)
        } else {
            try {
                val priority = Priority.PRIORITY_HIGH_ACCURACY
                // Check is the location settings is enabled
                val enabled = context.isLocationEnabled(priority)
                if (enabled) {
                    val locationClient = LocationServices.getFusedLocationProviderClient(context)

                    locationClient.lastLocation.await()?.let { lastLocation ->
                        location = lastLocation
                        onLocationFound(lastLocation)
                    }

                    locationClient.getCurrentLocation(priority, null).await()?.let { currentLocation ->
                        location = currentLocation
                        onLocationFound(currentLocation)
                    }
                }
            } catch (cause: ResolvableApiException) {
                // Show the system dialog to enable location
                launcher.launch(
                    IntentSenderRequest.Builder(cause.resolution.intentSender).build(),
                )
            }
        }
    }

    if (location != null) {
        MapWebView(
            modifier = modifier,
            latitude = location!!.latitude,
            longitude = location!!.longitude,
        )
    } else if (showLocationPermissionRequired) {
        LocationPermissionRequired(
            modifier = modifier,
            onClick = {
                context.openSystemSettings()
                onDismiss()
            },
        )
    } else {
        LoadingIndicator(modifier = modifier)
    }
}

@Composable
private fun LocationPermissionRequired(
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val title = "Location Permission Required"
    val message = "Location permission is required to find your current location"

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            style = ChatTheme.typography.title3Bold,
            text = title,
            textAlign = TextAlign.Center,
            color = ChatTheme.colors.textHighEmphasis,
        )

        Text(
            style = ChatTheme.typography.body,
            text = message,
            textAlign = TextAlign.Center,
            color = ChatTheme.colors.textLowEmphasis,
        )

        TextButton(
            colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.primaryAccent),
            onClick = onClick,
        ) {
            Text(stringResource(id = R.string.stream_compose_grant_permission))
        }
    }
}

@Composable
private fun LocationButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    icon: ImageVector,
    label: String,
    description: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(7.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isPrimary) {
                if (enabled) {
                    ChatTheme.colors.primaryAccent
                } else {
                    ChatTheme.colors.disabled
                }
            } else {
                ChatTheme.colors.borders
            },
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
                tint = if (enabled) {
                    ChatTheme.colors.primaryAccent
                } else {
                    ChatTheme.colors.disabled
                },
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = label,
                    style = ChatTheme.typography.title3Bold,
                )
                Text(
                    text = description,
                    style = ChatTheme.typography.footnote,
                    color = ChatTheme.colors.textLowEmphasis,
                )
            }
        }
    }
}

@Suppress("TooGenericExceptionCaught", "SwallowedException")
/**
 * Checks if the location settings is enabled.
 *
 * @throws ResolvableApiException
 */
private suspend fun Context.isLocationEnabled(priority: Int): Boolean {
    val client = LocationServices.getSettingsClient(this)
    val locationRequest = LocationRequest.Builder(priority, 0).build()
    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true)

    return try {
        client.checkLocationSettings(builder.build()).await()
        true
    } catch (e: ResolvableApiException) {
        // Caller will handle resolution
        throw e
    } catch (e: Exception) {
        false
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationPickerContentPreview() {
    ChatTheme {
        LocationPickerContent()
    }
}
