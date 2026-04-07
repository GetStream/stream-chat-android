/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.parser2.event

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.chat.android.models.MessageModerationAction
import io.getstream.chat.android.models.MessageModerationDetails

internal class MessageModerationDetailsAdapter : JsonAdapter<MessageModerationDetails>() {
    override fun fromJson(reader: JsonReader): MessageModerationDetails? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var originalText: String? = null
        var action: String? = null
        var errorMsg: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "original_text" -> originalText = reader.nextString()
                "action" -> action = reader.nextString()
                "error_msg" -> errorMsg = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return MessageModerationDetails(
            originalText = originalText.orEmpty(),
            action = MessageModerationAction.fromRawValue(action.orEmpty()),
            errorMsg = errorMsg.orEmpty(),
        )
    }

    override fun toJson(p0: JsonWriter, p1: MessageModerationDetails?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
