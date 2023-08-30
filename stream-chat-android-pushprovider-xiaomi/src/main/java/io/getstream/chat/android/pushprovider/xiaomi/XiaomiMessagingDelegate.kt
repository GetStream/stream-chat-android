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

package io.getstream.chat.android.pushprovider.xiaomi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.xiaomi.mipush.sdk.MiPushClient
import com.xiaomi.mipush.sdk.MiPushCommandMessage
import com.xiaomi.mipush.sdk.MiPushMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.PayloadValidator
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.models.PushProvider
import io.getstream.chat.android.client.notifications.parser.StreamPayloadParser
import kotlin.jvm.Throws

/**
 * Helper class for delegating Xiaomi push messages to the Stream Chat SDK.
 */
public object XiaomiMessagingDelegate {

    internal var fallbackProviderName: String? = null

    private val mapAdapter: JsonAdapter<MutableMap<String, String>> by lazy {
        Moshi.Builder()
            .build()
            .adapter(Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))
    }

    /**
     * Handles [miPushMessage] from Xiaomi.
     * If the [miPushMessage] wasn't sent from the Stream Server and doesn't contain the needed data,
     * return false to notify you that this remoteMessage needs to be handled by you.
     *
     * @param miPushMessage The message to be handled.
     * @return True if the [miPushMessage] was sent from the Stream Server and has been handled.
     *
     * @throws IllegalStateException If called before initializing ChatClient.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    public fun handleMiPushMessage(miPushMessage: MiPushMessage): Boolean {
        if (!miPushMessage.isValid()) {
            return false
        }

        ChatClient.handlePushMessage(miPushMessage.toPushMessage())
        return true
    }

    /**
     * Register new Xiaomi Token.
     *
     * @param miPushCommandMessage provided by Xiaomi.
     * @param providerName Optional name for the provider name.
     *
     * @throws IllegalStateException if called before initializing ChatClient.
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    public fun registerXiaomiToken(
        miPushCommandMessage: MiPushCommandMessage,
        providerName: String? = fallbackProviderName,
    ) {
        miPushCommandMessage
            .takeIf { it.command == MiPushClient.COMMAND_REGISTER }
            ?.commandArguments
            ?.get(0)
            ?.run {
                ChatClient.setDevice(
                    Device(
                        token = this,
                        pushProvider = PushProvider.XIAOMI,
                        providerName = providerName,
                    )
                )
            }
    }

    /**
     * Transform [MiPushMessage.content] into a [Map].
     *
     * Return a [Map] obtained from the value of [MiPushMessage.content] or an empty map if content was empty.
     */
    private val MiPushMessage.contentMap: Map<String, String>
        get() = mapAdapter.fromJson(content) ?: emptyMap()

    private fun MiPushMessage.toPushMessage() =
        contentMap.let {
            val expectedKeys = hashSetOf("channel_id", "message_id", "channel_type", "getstream")
            PushMessage(
                channelId = it["channel_id"]!!,
                messageId = it["message_id"]!!,
                channelType = it["channel_type"]!!,
                getstream = StreamPayloadParser.parse(it["getstream"]),
                extraData = it.filterKeys { key -> key !in expectedKeys },
                metadata = extractMetadata(),
            )
        }

    private fun MiPushMessage.extractMetadata(): Map<String, Any> {
        return hashMapOf<String, Any>().apply {
            put("xiaomi.message_type", messageType)
            messageId?.also { put("xiaomi.message_id", it) }
            userAccount?.also { put("xiaomi.user_account", it) }
            title?.also { put("xiaomi.title", it) }
            topic?.also { put("xiaomi.topic", it) }
            alias?.also { put("xiaomi.alias", it) }
            category?.also { put("xiaomi.category", it) }
            description?.also { put("xiaomi.description", it) }
            put("xiaomi.is_arrived_message", isArrivedMessage)
            put("xiaomi.is_notified", isNotified)
            put("xiaomi.notify_type", notifyType)
            put("xiaomi.notify_id", notifyId)
            put("xiaomi.pass_through", passThrough)
            put("xiaomi.extra", extra)
        }
    }

    private fun MiPushMessage.isValid() =
        PayloadValidator.isFromStreamServer(contentMap) &&
            PayloadValidator.isValidNewMessage(contentMap)
}
