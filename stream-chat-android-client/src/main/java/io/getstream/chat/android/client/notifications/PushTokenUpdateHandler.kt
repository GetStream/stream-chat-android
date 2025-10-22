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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger

/**
 * Manages the lifecycle of push notification devices for the current user.
 *
 * This handler is responsible for registering and unregistering push notification devices
 * with the Stream Chat backend. It tracks the currently active device and ensures that
 * device state stays synchronized with the server, avoiding duplicate registrations.
 *
 * The handler skips operations when:
 * - A device is already registered for the user (during [addDevice])
 * - No current device exists (during [deleteDevice])
 *
 */
internal class PushTokenUpdateHandler {

    private val logger by taggedLogger("Chat:Notifications-UH")

    private val chatClient: ChatClient get() = ChatClient.instance()

    private var currentDevice: Device? = null

    /**
     * Registers a new push notification device for the current user.
     *
     * This method attempts to add a device to the server if it is not already registered.
     * Before sending the request, it checks whether the device is already in the user's
     * registered devices list, and if so, skips the registration to avoid redundant operations.
     *
     * Upon successful registration, [currentDevice] is updated to track the newly added device.
     * Upon failure, the operation is logged but does not rethrow the error.
     *
     * @param user The current user, or `null` if no user is logged in. Used to check if the
     *             device is already registered. If `null`, the device will be treated as
     *             unregistered.
     * @param device The device to register. Must contain a valid token and push provider.
     *
     * **Behavior**:
     * - If the device is already registered (found in [user.devices]), logs a message and returns early.
     * - If not registered, sends an add device request to the server.
     * - On success: updates [currentDevice] and logs the device token.
     * - On error: logs the failure but does not propagate the exception.
     */
    suspend fun addDevice(user: User?, device: Device) {
        val isDeviceRegistered = isDeviceRegistered(user, device)
        if (isDeviceRegistered) {
            logger.d { "[addDevice] skip adding device: already registered on server" }
            currentDevice = device
            return
        }
        chatClient.addDevice(device).await()
            .onSuccess {
                currentDevice = device
                logger.d { "[addDevice] successfully added ${device.pushProvider.key} device ${device.token}" }
            }
            .onError {
                logger.d { "[addDevice] failed to add ${device.pushProvider.key} device ${device.token}" }
            }
    }

    /**
     * Unregisters the currently tracked push notification device from the server.
     *
     * This method attempts to delete the device that is stored in [currentDevice].
     * If no device is currently tracked, the operation is skipped.
     *
     * Upon successful deletion, [currentDevice] is cleared to reflect that no device
     * is currently registered. Upon failure, the operation is logged but does not
     * rethrow the error, and [currentDevice] remains unchanged.
     *
     * **Behavior**:
     * - If [currentDevice] is `null`, logs a message and returns early.
     * - If a device is tracked, sends a delete device request to the server.
     * - On success: clears [currentDevice] and logs the device token.
     * - On error: logs the failure but does not propagate the exception.
     */
    suspend fun deleteDevice() {
        val device = currentDevice
        if (device == null) {
            logger.d { "[deleteDevice] skip deleting device: no current device" }
            return
        }
        chatClient.deleteDevice(device).await()
            .onSuccess {
                currentDevice = null
                logger.d { "[deleteDevice] successfully deleted ${device.pushProvider.key} device ${device.token}" }
            }
            .onError {
                logger.d { "[deleteDevice] failed to delete ${device.pushProvider.key} device ${device.token}" }
            }
    }

    private fun isDeviceRegistered(user: User?, device: Device): Boolean {
        val registeredDevices = user?.devices ?: return false
        return registeredDevices.any { it == device }
    }
}
