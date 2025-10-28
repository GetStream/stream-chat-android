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

import io.getstream.android.push.PushDevice
import io.getstream.android.push.PushProvider
import io.getstream.android.push.delegate.PushDelegate
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.PayloadValidator
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.notifications.parser.StreamPayloadParser
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.PushMessage

private typealias DevicePushProvider = io.getstream.chat.android.models.PushProvider

/**
 * Internal class that handle PN stuff.
 * It is declared in our Android Manifest and is used by reflection.
 */
@Suppress("Unused")
internal class ChatPushDelegate : PushDelegate() {

    private val expectedKeys = hashSetOf(KEY_CHANNEL_ID, KEY_MESSAGE_ID, KEY_CHANNEL_TYPE, KEY_GETSTREAM)

    override fun handlePushMessage(metadata: Map<String, Any?>, payload: Map<String, Any?>): Boolean = payload.ifValid {
        ChatClient.handlePushMessage(
            PushMessage(
                channelType = extractChannelType(payload),
                channelId = extractChannelId(payload),
                messageId = payload.getValue(KEY_MESSAGE_ID) as String,
                getstream = StreamPayloadParser.parse(payload[KEY_GETSTREAM] as? String),
                extraData = payload.filterKeys { it !in expectedKeys },
                metadata = metadata,
            ),
        )
    }

    override fun registerPushDevice(pushDevice: PushDevice) {
        ChatClient.setDevice(pushDevice.toDevice())
    }

    private fun Map<String, Any?>.ifValid(effect: () -> Unit): Boolean {
        val isValid = PayloadValidator.isFromStreamServer(this) &&
            PayloadValidator.isValidPayload(this)
        effect.takeIf { isValid }?.invoke()
        return isValid
    }

    private fun extractChannelType(payload: Map<String, Any?>): String {
        val cid = payload[KEY_CID] as? String
        if (cid != null) {
            val (channelType, _) = cid.cidToTypeAndId()
            return channelType
        }
        return payload[KEY_CHANNEL_TYPE] as String
    }

    private fun extractChannelId(payload: Map<String, Any?>): String {
        val cid = payload[KEY_CID] as? String
        if (cid != null) {
            val (_, channelId) = cid.cidToTypeAndId()
            return channelId
        }
        return payload[KEY_CHANNEL_ID] as String
    }

    private companion object {
        private const val KEY_CHANNEL_ID = "channel_id"
        private const val KEY_MESSAGE_ID = "message_id"
        private const val KEY_CHANNEL_TYPE = "channel_type"
        private const val KEY_CID = "cid"
        private const val KEY_GETSTREAM = "getstream"
    }
}

internal fun PushDevice.toDevice(): Device = Device(
    token = token,
    pushProvider = pushProvider.toDevicePushProvider(),
    providerName = providerName,
)

private fun PushProvider.toDevicePushProvider(): DevicePushProvider = when (this) {
    PushProvider.FIREBASE -> DevicePushProvider.FIREBASE
    PushProvider.HUAWEI -> DevicePushProvider.HUAWEI
    PushProvider.XIAOMI -> DevicePushProvider.XIAOMI
    PushProvider.UNKNOWN -> DevicePushProvider.UNKNOWN
}
