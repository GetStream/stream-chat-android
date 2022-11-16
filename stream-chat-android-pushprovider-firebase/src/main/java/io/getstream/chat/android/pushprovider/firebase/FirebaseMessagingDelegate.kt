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

package io.getstream.chat.android.pushprovider.firebase

import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.PayloadValidator
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.models.PushProvider

/**
 * Helper class for delegating Firebase push messages to the Stream Chat SDK.
 */
public object FirebaseMessagingDelegate {

    internal var fallbackProviderName: String? = null

    /**
     * Handles [remoteMessage] from Firebase.
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
     * Register new Firebase Token.
     *
     * @param token provided by Firebase.
     * @param providerName Optional name for the provider name.
     *
     * @throws IllegalStateException if called before initializing ChatClient.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    public fun registerFirebaseToken(
        token: String,
        providerName: String? = fallbackProviderName,
    ) {
        ChatClient.setDevice(
            Device(
                token = token,
                pushProvider = PushProvider.FIREBASE,
                providerName = providerName,
            )
        )
    }
}

private fun RemoteMessage.toPushMessage() =
    PushMessage(
        channelId = data.getValue("channel_id"),
        messageId = data.getValue("message_id"),
        channelType = data.getValue("channel_type"),
    )

private fun RemoteMessage.isValid() =
    PayloadValidator.isFromStreamServer(data) &&
        PayloadValidator.isValidNewMessage(data)
