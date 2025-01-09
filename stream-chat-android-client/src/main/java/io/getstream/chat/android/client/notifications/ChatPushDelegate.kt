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
import io.getstream.android.push.PushDevice
import io.getstream.android.push.PushProvider
import io.getstream.android.push.delegate.PushDelegate
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.PayloadValidator
import io.getstream.chat.android.client.notifications.parser.StreamPayloadParser
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.PushMessage

private typealias DevicePushProvider = io.getstream.chat.android.models.PushProvider

/**
 * Internal class that handle PN stuff.
 * It is declared in our Android Manifest and is used by reflection.
 *
 * IMPORTANT: Don't remove the context argument even though it is not used. This class is instantiated via reflection,
 * and it expects a constructor with single Context parameter.
 */
@Suppress("Unused")
internal class ChatPushDelegate(context: Context) : PushDelegate() {

    private val expectedKeys = hashSetOf(KEY_CHANNEL_ID, KEY_MESSAGE_ID, KEY_CHANNEL_TYPE, KEY_GETSTREAM)

    override fun handlePushMessage(metadata: Map<String, Any?>, payload: Map<String, Any?>): Boolean =
        payload.ifValid {
            ChatClient.handlePushMessage(
                PushMessage(
                    channelId = payload.getValue(KEY_CHANNEL_ID) as String,
                    messageId = payload.getValue(KEY_MESSAGE_ID) as String,
                    channelType = payload.getValue(KEY_CHANNEL_TYPE) as String,
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
            PayloadValidator.isValidNewMessage(this)
        effect.takeIf { isValid }?.invoke()
        return isValid
    }

    private companion object {
        private const val KEY_CHANNEL_ID = "channel_id"
        private const val KEY_MESSAGE_ID = "message_id"
        private const val KEY_CHANNEL_TYPE = "channel_type"
        private const val KEY_GETSTREAM = "getstream"
    }
}

internal fun PushDevice.toDevice(): Device =
    Device(
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
