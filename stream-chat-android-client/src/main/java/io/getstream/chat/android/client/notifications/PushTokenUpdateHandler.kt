/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.notifications

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Handles the registration and deregistration of push notification devices for users.
 *
 * This class manages the lifecycle of push notification tokens by:
 * - Registering devices with the Stream backend when a push token is obtained
 * - Preventing duplicate device registrations within the same session
 * - Tracking registered devices per user to avoid unnecessary API calls
 * - Ensuring thread-safe device registration operations
 * - Removing devices when users log out
 *
 * ## Device Registration Logic
 *
 * The handler implements smart registration logic to minimize API calls:
 * 1. Checks if a device is already registered for the current user (either remotely or in-session)
 * 2. Skips registration if the device is already known
 * 3. Otherwise, registers the device with the backend
 *
 * ## Thread Safety
 *
 * All device operations are protected by a mutex to ensure that concurrent registration attempts
 * from multiple coroutines don't result in race conditions or duplicate API calls.
 *
 * ## Session Tracking
 *
 * The handler maintains an in-memory map of registered devices per user session. This ensures that:
 * - Multiple calls to register the same device don't result in redundant API requests
 * - The handler can efficiently determine device registration status
 * - Each user can have only one tracked device per session
 *
 * @param api The [ChatApi] instance used to communicate with the Stream backend for device operations.
 *
 * @see Device
 * @see ChatApi.addDevice
 * @see ChatApi.deleteDevice
 */
internal class PushTokenUpdateHandler(private val api: ChatApi) {

    private val logger by taggedLogger("Chat:Notifications-UH")

    /**
     * Mutex to ensure thread-safe device registration operations.
     * Prevents race conditions when multiple coroutines attempt to register devices simultaneously.
     */
    private val addDeviceLock = Mutex()

    /**
     * Tracks registered devices per user in the current session.
     *
     * This map serves two purposes:
     * 1. Keeps track of devices registered during this app session
     * 2. Records devices that were already registered on the backend but were attempted
     *    to be registered again in this session (to prevent redundant API calls)
     *
     * **Key:** User ID
     * **Value:** The [Device] registered for that user
     *
     * Note: There can only be one device per user per session, as each session typically
     * corresponds to a single device/app instance.
     */
    @VisibleForTesting
    internal val registeredDeviceInSession: MutableMap<String, Device> = mutableMapOf()

    /**
     * Registers a push notification device for the specified user.
     *
     * This method handles the registration of a push notification device with the Stream backend.
     * It implements several optimizations and safety checks:
     *
     * ## Registration Flow
     * 1. Validates that a user is provided (returns early if null)
     * 2. Acquires a lock to ensure thread-safe operation
     * 3. Checks if the device is already registered (locally or remotely)
     * 4. Skips registration if the device is already known
     * 5. Otherwise, calls the backend API to register the device
     * 6. Tracks the device in the session map upon successful registration
     *
     * ## Duplicate Prevention
     * The method prevents duplicate registrations by checking:
     * - Devices already registered in the current session (via [registeredDeviceInSession])
     * - Devices already registered on the backend (via [User.devices])
     *
     * ## Thread Safety
     * Uses [addDeviceLock] to ensure only one registration operation occurs at a time,
     * preventing race conditions when multiple coroutines attempt to register simultaneously.
     *
     * ## Error Handling
     * - Logs and continues execution if the user is null
     * - Logs errors from the API but doesn't throw exceptions (fire-and-forget pattern)
     * - Updates tracking only on successful registration
     *
     * @param user The [User] for whom to register the device. If null, registration is skipped.
     * @param device The [Device] to register, containing the push token and device metadata.
     *
     * @see isDeviceRegistered
     * @see ChatApi.addDevice
     */
    suspend fun addDevice(user: User?, device: Device) {
        // If user is null, we cannot add the device
        if (user == null) {
            logger.d { "[addDevice] User is null, cannot add device ${device.token}" }
            return
        }
        // Ensure only one addDevice operation is happening at a time
        addDeviceLock.withLock {
            // Check if the device is already registered for the user
            val isDeviceRegistered = isDeviceRegistered(user, device)
            if (isDeviceRegistered) {
                // Device is already registered for the user, skip adding it again
                registeredDeviceInSession[user.id] = device
                logger.d { "[addDevice] Device ${device.token} is already registered for ${user.id}, skipping." }
                return
            }
            // Proceed to add the device
            api.addDevice(device).await()
                .onSuccess {
                    registeredDeviceInSession[user.id] = device
                    logger.d { "[addDevice] Successfully added device ${device.token} for ${user.id}" }
                }
                .onError { error ->
                    logger.e { "[addDevice] Failed to add device ${device.token} for ${user.id}, $error" }
                }
        }
    }

    /**
     * Removes the registered push notification device for the specified user.
     *
     * This method handles the deregistration of a push notification device from the Stream backend,
     * called when a user logs out.
     *
     * ## Deletion Flow
     * 1. Validates that a user is provided (returns early if null)
     * 2. Checks if a device is tracked for the user in the current session
     * 3. Skips deletion if no device is tracked (nothing to delete)
     * 4. Calls the backend API to delete the device
     * 5. Removes the device from session tracking upon successful deletion
     *
     * ## Session Tracking
     * Only devices tracked in [registeredDeviceInSession] can be deleted. This ensures that:
     * - The handler only attempts to delete devices it knows about
     * - Prevents unnecessary API calls for devices not registered (or attempted to be registered) in this session
     * - Maintains consistency between local state and backend state
     *
     * ## Error Handling
     * - Logs and returns early if the user is null
     * - Logs and returns early if no device is tracked for the user
     * - Logs errors from the API but doesn't throw exceptions (fire-and-forget pattern)
     * - Removes from tracking only on successful deletion
     *
     * @param user The [User] whose device should be removed. If null, deletion is skipped.
     *
     * @see addDevice
     * @see ChatApi.deleteDevice
     */
    suspend fun deleteDevice(user: User?) {
        if (user == null) {
            logger.d { "[deleteDevice] User is null, cannot delete device." }
            return
        }
        val device = registeredDeviceInSession[user.id]
        if (device == null) {
            logger.d { "[deleteDevice] No device tracked in session for user ${user.id}, skipping deletion." }
            return
        }
        api.deleteDevice(device.token).await()
            .onSuccess {
                registeredDeviceInSession.remove(user.id)
                logger.d { "[deleteDevice] Successfully deleted device ${device.token} for ${user.id}" }
            }
            .onError { error ->
                logger.e { "[deleteDevice] Failed to delete device ${device.token} for ${user.id}, $error" }
            }
    }

    /**
     * Determines whether a device is already registered for the specified user.
     *
     * This method checks device registration status from two sources:
     *
     * ## Check 1: Remote Devices (Backend State)
     * Examines the [User.devices] list, which contains devices registered on the backend.
     * This list is typically populated when the user connects or from user objects received
     * from the backend.
     *
     * ## Check 2: Session Tracking (Local State)
     * Checks [registeredDeviceInSession] to see if the device was registered during the current
     * app session. This catches cases where:
     * - The device was just registered in this session
     * - The user object hasn't been refreshed yet to include the new device
     * - Prevents redundant registration attempts before the backend state updates
     *
     * ## Registration Logic
     * A device is considered registered if it appears in **either**:
     * - The user's remote devices list (backend state)
     * - The session tracking map (local state)
     *
     * This dual-check approach ensures accurate registration status even when there's a delay
     * between local registration and backend state synchronization.
     *
     * @param user The [User] to check device registration for.
     * @param device The [Device] to check registration status of.
     * @return `true` if the device is registered (remotely or in-session), `false` otherwise.
     *
     * @see addDevice
     * @see User.devices
     */
    private fun isDeviceRegistered(user: User, device: Device): Boolean {
        val remoteDevices = user.devices
        val deviceRegisteredInSession = registeredDeviceInSession[user.id]
        return remoteDevices.contains(device) || deviceRegisteredInSession == device
    }
}
