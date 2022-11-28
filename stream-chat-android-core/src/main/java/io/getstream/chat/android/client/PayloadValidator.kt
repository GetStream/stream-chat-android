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

    private const val KEY_SENDER_V1 = "sender_server"
    private const val KEY_SENDER = "sender"
    private const val KEY_VERSION = "version"
    private const val KEY_TYPE = "type"
    private const val KEY_CHANNEL_ID = "channel_id"
    private const val KEY_MESSAGE_ID = "message_id"
    private const val KEY_CHANNEL_TYPE = "channel_type"

    private const val VALUE_STREAM_SENDER = "stream.chat"
    private const val VALUE_NEW_MESSAGE_TYPE = "message.new"
    private const val VALUE_V1 = "v1"
    private const val VALUE_V2 = "v2"

    /**
     * Verify the payload comes from Stream Server.
     *
     * @return true if the payload comes from Stream Server.
     */
    public fun isFromStreamServer(payload: Map<String, Any?>): Boolean = when (payload[KEY_VERSION]) {
        VALUE_V1 -> isFromStreamServerV1(payload)
        VALUE_V2 -> isFromStreamServerV2(payload)
        else -> false
    }

    /**
     * Verify the payload comes from Stream Server using v1 fields.
     *
     * @return true if the payload comes from Stream Server.
     */
    private fun isFromStreamServerV1(payload: Map<String, Any?>): Boolean =
        payload[KEY_SENDER_V1] == VALUE_STREAM_SENDER

    /**
     * Verify the payload comes from Stream Server using v2 fields.
     *
     * @return true if the payload comes from Stream Server.
     */
    private fun isFromStreamServerV2(payload: Map<String, Any?>): Boolean =
        payload[KEY_SENDER] == VALUE_STREAM_SENDER

    /**
     * Verify the payload contains needed field for a new message.
     *
     * @return true if the payload contains all needed fields for a new message.
     */
    public fun isValidNewMessage(payload: Map<String, Any?>): Boolean = when (payload[KEY_VERSION]) {
        VALUE_V1 -> isValidNewMessageV1(payload)
        VALUE_V2 -> isValidNewMessageV2(payload)
        else -> false
    }

    /**
     * Verify the payload contains needed field for a new message using v1 fields.
     *
     * @return true if the payload contains all needed fields for a new message.
     */
    private fun isValidNewMessageV1(payload: Map<String, Any?>): Boolean =
        !(payload[KEY_CHANNEL_ID] as? String).isNullOrBlank() &&
            !(payload[KEY_MESSAGE_ID] as? String).isNullOrBlank() &&
            !(payload[KEY_CHANNEL_TYPE] as? String).isNullOrBlank()

    /**
     * Verify the payload contains needed field for a new message using v2 fields.
     *
     * @return true if the payload contains all needed fields for a new message.
     */
    private fun isValidNewMessageV2(payload: Map<String, Any?>): Boolean =
        payload[KEY_TYPE] == VALUE_NEW_MESSAGE_TYPE &&
            !(payload[KEY_CHANNEL_ID] as? String).isNullOrBlank() &&
            !(payload[KEY_MESSAGE_ID] as? String).isNullOrBlank() &&
            !(payload[KEY_CHANNEL_TYPE] as? String).isNullOrBlank()
}
