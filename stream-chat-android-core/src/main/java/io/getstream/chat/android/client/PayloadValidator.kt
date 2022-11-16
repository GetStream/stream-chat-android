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

package io.getstream.chat.android.client

public object PayloadValidator {

    private const val KEY_SENDER = "sender"
    private const val KEY_TYPE = "type"
    private const val KEY_CHANNEL_ID = "channel_id"
    private const val KEY_MESSAGE_ID = "message_id"
    private const val KEY_CHANNEL_TYPE = "channel_type"

    private const val VALUE_STREAM_SENDER = "stream.chat"
    private const val VALUE_NEW_MESSAGE_TYPE = "message.new"

    public fun isFromStreamServer(payload: Map<String, Any?>): Boolean = payload[KEY_SENDER] == VALUE_STREAM_SENDER

    public fun isValidNewMessage(payload: Map<String, Any?>): Boolean =
        payload[KEY_TYPE] == VALUE_NEW_MESSAGE_TYPE &&
            !(payload[KEY_CHANNEL_ID] as? String).isNullOrBlank() &&
            !(payload[KEY_MESSAGE_ID] as? String).isNullOrBlank() &&
            !(payload[KEY_CHANNEL_TYPE] as? String).isNullOrBlank()
}
