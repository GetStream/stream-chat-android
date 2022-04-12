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

package io.getstream.chat.android.pushprovider.huawei

import com.huawei.hms.push.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.models.PushProvider
import kotlin.jvm.Throws

/**
 * Helper class for delegating Huawei push messages to the Stream Chat SDK.
 */
public object HuaweiMessagingDelegate {

    /**
     * Handles [remoteMessage] from Huawei.
     * If the [remoteMessage] wasn't sent from the Stream Server and doesn't contain the needed data,
     * return false to notify you that this remoteMessage needs to be handled by you.
     *
     * @param remoteMessage The message to be handled.
     * @return True if the [remoteMessage] was sent from the Stream Server and has been handled.
     *
     * @throws IllegalStateException If called before initializing ChatClient.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    public fun handleRemoteMessage(remoteMessage: RemoteMessage): Boolean {
        if (!remoteMessage.isValid()) {
            return false
        }

        ChatClient.handlePushMessage(remoteMessage.toPushMessage())
        return true
    }

    /**
     * Register new Huawei Token.
     *
     * @param token provided by Huawei.
     *
     * @throws IllegalStateException if called before initializing ChatClient.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    public fun registerHuaweiToken(token: String) {
        ChatClient.setDevice(
            Device(
                token = token,
                pushProvider = PushProvider.HUAWEI,
            )
        )
    }
}

private fun RemoteMessage.toPushMessage() =
    PushMessage(
        channelId = dataOfMap["channel_id"]!!,
        messageId = dataOfMap["message_id"]!!,
        channelType = dataOfMap["channel_type"]!!,
    )

private fun RemoteMessage.isValid() =
    !dataOfMap["channel_id"].isNullOrBlank() &&
        !dataOfMap["message_id"].isNullOrBlank() &&
        !dataOfMap["channel_type"].isNullOrBlank()
