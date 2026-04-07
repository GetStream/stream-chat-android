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
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.chat.android.models.MessageReminderInfo
import java.util.Date

internal class MessageReminderInfoAdapter(
    private val dateAdapter: JsonAdapter<Date>,
) : JsonAdapter<MessageReminderInfo>() {
    override fun fromJson(reader: JsonReader): MessageReminderInfo? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var remindAt: Date? = null
        var createdAt: Date? = null
        var updatedAt: Date? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "remind_at" -> remindAt = dateAdapter.fromJson(reader)
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (createdAt == null) {
            throw JsonDataException("Required value 'created_at' missing at ${reader.path}")
        }
        if (updatedAt == null) {
            throw JsonDataException("Required value 'updated_at' missing at ${reader.path}")
        }

        return MessageReminderInfo(
            remindAt = remindAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    override fun toJson(p0: JsonWriter, p1: MessageReminderInfo?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
