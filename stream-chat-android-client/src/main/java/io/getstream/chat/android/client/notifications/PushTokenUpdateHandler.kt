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

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.extensions.getNonNullString
import io.getstream.chat.android.core.utils.Debouncer
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.PushProvider
import io.getstream.log.taggedLogger

internal class PushTokenUpdateHandler(context: Context) {
    private val logger by taggedLogger("Chat:Notifications-UH")

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    private val chatClient: ChatClient get() = ChatClient.instance()

    private val updateDebouncer = Debouncer(DEBOUNCE_TIMEOUT)

    private var userPushToken: UserPushToken
        set(value) {
            prefs.edit(true) {
                putString(KEY_USER_ID, value.userId)
                putString(KEY_TOKEN, value.token)
                putString(KEY_PUSH_PROVIDER, value.pushProvider)
                putString(KEY_PUSH_PROVIDER_NAME, value.providerName)
            }
        }
        get() {
            return UserPushToken(
                userId = prefs.getNonNullString(KEY_USER_ID, ""),
                token = prefs.getNonNullString(KEY_TOKEN, ""),
                pushProvider = prefs.getNonNullString(KEY_PUSH_PROVIDER, ""),
                providerName = prefs.getString(KEY_PUSH_PROVIDER_NAME, null),
            )
        }

    /**
     * Registers the current device on the server if necessary. Does no do
     * anything if the token has already been sent to the server previously.
     */
    suspend fun updateDeviceIfNecessary(device: Device) {
        val userPushToken = device.toUserPushToken()
        if (!device.isValid()) return
        if (this.userPushToken == userPushToken) {
            logger.d { "[updateDeviceIfNecessary] skip update device: same as previous" }
            return
        }
        updateDebouncer.submitSuspendable {
            logger.d { "[updateDeviceIfNecessary] device: $device" }
            val removed = removeStoredDeviceInternal()
            logger.v { "[updateDeviceIfNecessary] removed: $removed" }
            val result = chatClient.addDevice(device).await()
            if (result.isSuccess) {
                this.userPushToken = userPushToken
                val pushProvider = device.pushProvider.key
                logger.i { "[updateDeviceIfNecessary] device registered with token($pushProvider): ${device.token}" }
            } else {
                logger.e { "[updateDeviceIfNecessary] failed registering device ${result.errorOrNull()?.message}" }
            }
        }
    }

    suspend fun removeStoredDevice() {
        logger.v { "[removeStoredDevice] no args" }
        val removed = removeStoredDeviceInternal()
        logger.i { "[removeStoredDevice] removed: $removed" }
    }

    private suspend fun removeStoredDeviceInternal(): Boolean {
        val device = userPushToken.toDevice()
            .takeIf { it.isValid() }
            ?: return false
        userPushToken = UserPushToken("", "", "", null)
        return chatClient.deleteDevice(device).await()
            .onError {
                if (!it.isPermanent()) {
                    userPushToken = device.toUserPushToken()
                }
                logger.e { "[removeStoredDeviceInternal] failed: $it" }
            }
            .isSuccess
    }

    private data class UserPushToken(
        val userId: String,
        val token: String,
        val pushProvider: String,
        val providerName: String?,
    )

    companion object {
        private const val PREFS_NAME = "stream_firebase_token_store"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN = "token"
        private const val KEY_PUSH_PROVIDER = "push_provider"
        private const val KEY_PUSH_PROVIDER_NAME = "push_provider_name"
        private const val DEBOUNCE_TIMEOUT = 200L
    }

    private fun Device.toUserPushToken() = UserPushToken(
        userId = chatClient.getCurrentUser()?.id ?: "",
        token = token,
        pushProvider = pushProvider.key,
        providerName = providerName,
    )

    private fun UserPushToken.toDevice() = Device(
        token = token,
        pushProvider = PushProvider.fromKey(pushProvider),
        providerName = providerName,
    )

    private fun Device.isValid() = pushProvider != PushProvider.UNKNOWN
}
