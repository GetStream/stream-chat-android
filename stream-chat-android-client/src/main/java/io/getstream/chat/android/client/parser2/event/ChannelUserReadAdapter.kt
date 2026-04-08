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
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.User
import java.util.Date

internal class ChannelUserReadAdapter(
    private val userAdapter: JsonAdapter<User>,
    private val dateAdapter: JsonAdapter<Date>,
    private val lastReceivedEventDate: Date,
) : JsonAdapter<ChannelUserRead>() {
    override fun fromJson(reader: JsonReader): ChannelUserRead? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var user: User? = null
        var lastRead: Date? = null
        var unreadMessages: Int? = null
        var lastReadMessageId: String? = null
        var lastDeliveredAt: Date? = null
        var lastDeliveredMessageId: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "user" -> user = userAdapter.fromJson(reader)
                "last_read" -> lastRead = dateAdapter.fromJson(reader)
                "unread_messages" -> unreadMessages = reader.nextInt()
                "last_read_message_id" -> lastReadMessageId = reader.nextString()
                "last_delivered_at" -> lastDeliveredAt = dateAdapter.fromJson(reader)
                "last_delivered_message_id" -> lastDeliveredMessageId = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(user, "user", reader)
        JsonParsingUtils.requireField(lastRead, "last_read", reader)
        JsonParsingUtils.requireField(unreadMessages, "unread_messages", reader)

        return ChannelUserRead(
            user = user,
            lastReceivedEventDate = lastReceivedEventDate,
            lastRead = lastRead,
            unreadMessages = unreadMessages,
            lastReadMessageId = lastReadMessageId,
            lastDeliveredAt = lastDeliveredAt,
            lastDeliveredMessageId = lastDeliveredMessageId,
        )
    }

    override fun toJson(p0: JsonWriter, p1: ChannelUserRead?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
