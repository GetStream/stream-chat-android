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
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.extensions.getNonNullString
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushProvider

internal class PushTokenUpdateHandler(context: Context) {
    private val logger = ChatLogger.get("ChatNotifications")

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private var userPushToken: UserPushToken
        set(value) {
            prefs.edit()
                .putString(KEY_USER_ID, value.userId)
                .putString(KEY_TOKEN, value.token)
                .putString(KEY_PUSH_PROVIDER, value.pushProvider)
                .apply()
        }
        get() {
            return UserPushToken(
                userId = prefs.getNonNullString(KEY_USER_ID, ""),
                token = prefs.getNonNullString(KEY_TOKEN, ""),
                pushProvider = prefs.getNonNullString(KEY_PUSH_PROVIDER, ""),
            )
        }

    /**
     * Registers the current device on the server if necessary. Does no do
     * anything if the token has already been sent to the server previously.
     */
    suspend fun updateDeviceIfNecessary(device: Device) {
        val userPushToken = device.toUserPushToken()
        if (device.isValid() && this.userPushToken != userPushToken) {
            removeStoredDevice()
            val result = ChatClient.instance().addDevice(device).await()
            if (result.isSuccess) {
                this.userPushToken = userPushToken
                logger.logI("Device registered with token ${device.token} (${device.pushProvider.key})")
            } else {
                logger.logE("Error registering device ${result.error().message}")
            }
        }
    }

    suspend fun removeStoredDevice() {
        userPushToken.toDevice()
            .takeIf { it.isValid() }
            ?.let {
                if (ChatClient.instance().deleteDevice(it).await().isSuccess) {
                    userPushToken = UserPushToken("", "", "")
                }
            }
    }

    private data class UserPushToken(
        val userId: String,
        val token: String,
        val pushProvider: String,
    )

    companion object {
        private const val PREFS_NAME = "stream_firebase_token_store"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN = "token"
        private const val KEY_PUSH_PROVIDER = "push_provider"
    }

    private fun Device.toUserPushToken() = UserPushToken(
        userId = ChatClient.instance().getCurrentUser()?.id ?: "",
        token = token,
        pushProvider = pushProvider.key
    )

    private fun UserPushToken.toDevice() = Device(token = token, pushProvider = PushProvider.fromKey(pushProvider))

    private fun Device.isValid() = pushProvider != PushProvider.UNKNOWN
}
