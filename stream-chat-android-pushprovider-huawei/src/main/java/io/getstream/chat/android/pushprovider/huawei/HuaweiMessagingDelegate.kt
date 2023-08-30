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
import io.getstream.chat.android.client.PayloadValidator
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.models.PushProvider
import io.getstream.chat.android.client.notifications.parser.StreamPayloadParser
import kotlin.jvm.Throws

/**
 * Helper class for delegating Huawei push messages to the Stream Chat SDK.
 */
public object HuaweiMessagingDelegate {

    internal var fallbackProviderName: String? = null

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
     * @param providerName Optional name for the provider name.
     *
     * @throws IllegalStateException if called before initializing ChatClient.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    public fun registerHuaweiToken(
        token: String,
        providerName: String? = fallbackProviderName,
    ) {
        ChatClient.setDevice(
            Device(
                token = token,
                pushProvider = PushProvider.HUAWEI,
                providerName = providerName,
            )
        )
    }
}

private fun RemoteMessage.toPushMessage(): PushMessage {
    val expectedKeys = hashSetOf("channel_id", "message_id", "channel_type", "getstream")
    return PushMessage(
        channelId = dataOfMap["channel_id"]!!,
        messageId = dataOfMap["message_id"]!!,
        channelType = dataOfMap["channel_type"]!!,
        getstream = StreamPayloadParser.parse(dataOfMap["getstream"]),
        extraData = dataOfMap.filterKeys { it !in expectedKeys },
        metadata = extractMetadata(),
    )
}

private fun RemoteMessage.extractMetadata(): Map<String, Any> {
    return hashMapOf<String, Any>().apply {
        from?.also { put("huawei.from", it) }
        to?.also { put("huawei.to", it) }
        messageType?.also { put("huawei.message_type", it) }
        messageId?.also { put("huawei.message_id", it) }
        collapseKey?.also { put("huawei.collapse_key", it) }
        analyticInfoMap?.also { put("huawei.analytic_info_map", it) }
        token?.also { put("huawei.token", token) }
        put("huawei.sent_time", sentTime)
        put("huawei.send_mode", sendMode)
        put("huawei.receipt_mode", receiptMode)
        put("huawei.urgency", urgency)
        put("huawei.original_urgency", originalUrgency)
        put("huawei.original_urgency", originalUrgency)
        put("huawei.ttl", ttl)
    }
}

private fun RemoteMessage.isValid() =
    PayloadValidator.isFromStreamServer(dataOfMap) &&
        PayloadValidator.isValidNewMessage(dataOfMap)
