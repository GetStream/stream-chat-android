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

package io.getstream.chat.android.compose.sample.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.messaging.FirebaseMessaging
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.state.extensions.globalStateFlow
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * A simple class to handle shared location updates for simplicity.
 * Consider using a foreground service for continuous location updates while the app is in the background.
 */
class SharedLocationService(private val context: Context) : LocationCallback() {

    private val logger by taggedLogger("Chat:SharedLocationService")

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val chatClient by lazy { ChatClient.instance() }

    @Volatile
    private var activeLiveLocations: List<Location> = emptyList()

    // Track whether we are currently receiving updates
    @Volatile
    private var isReceivingUpdates = false

    var currentDeviceId: String = UnknownDevice
        private set

    fun start() {
        logger.d { "Starting..." }

        scope.launch { currentDeviceId = getCurrentDeviceId() }

        chatClient.queryActiveLocations().enqueue()

        chatClient.globalStateFlow
            .flatMapLatest { it.currentUserActiveLiveLocations }
            .onEach { currentUserActiveLiveLocations ->
                logger.d { "Current user active live locations: $currentUserActiveLiveLocations" }

                activeLiveLocations = currentUserActiveLiveLocations.toList()

                if (activeLiveLocations.isEmpty()) {
                    logger.d { "No active locations -> stopping location updates..." }
                    locationClient.removeLocationUpdates(this)
                    isReceivingUpdates = false
                } else {
                    if (!isReceivingUpdates) {
                        logger.d { "Active locations present -> starting location updates..." }

                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            val request = LocationRequest.Builder(
                                Priority.PRIORITY_HIGH_ACCURACY,
                                LocationUpdatesIntervalMillis,
                            ).build()
                            locationClient.requestLocationUpdates(request, this, Looper.getMainLooper())
                            isReceivingUpdates = true
                        } else {
                            logger.e { "Location permission not granted, cannot request location updates" }
                        }
                    } else {
                        logger.d { "Already receiving updates, no need to request again" }
                    }
                }
            }
            .catch { e ->
                logger.e(e) { "Error collecting currentUserActiveLiveLocations" }
            }
            .launchIn(scope)
    }

    override fun onLocationResult(result: LocationResult) {
        logger.d { "Location result received: ${result.locations}" }

        val locationsToUpdate = activeLiveLocations

        for (location in result.locations) {
            locationsToUpdate.forEach { activeLiveLocation ->
                chatClient.updateLiveLocation(
                    messageId = activeLiveLocation.messageId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    deviceId = currentDeviceId,
                ).enqueue { result ->
                    if (result is Result.Success) {
                        logger.d { "Live location updated successfully: ${result.value}" }
                    } else {
                        logger.e { "Failed to update live location: ${result.errorOrNull()}" }
                    }
                }
            }
        }
    }

    fun stop() {
        logger.d { "Stopping..." }
        locationClient.removeLocationUpdates(this)
        scope.cancel()
    }

    private suspend fun getCurrentDeviceId(): String = runCatching {
        FirebaseMessaging.getInstance()
            .token.await()
            .let { token ->
                chatClient.getCurrentUser()?.devices?.find { device ->
                    device.token == token
                }?.token
            } ?: UnknownDevice
    }.getOrElse { UnknownDevice }
}

private const val LocationUpdatesIntervalMillis = 5000L
private const val UnknownDevice = "unknown_device"
