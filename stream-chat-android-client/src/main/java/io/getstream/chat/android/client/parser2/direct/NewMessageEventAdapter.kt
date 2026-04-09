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

package io.getstream.chat.android.client.parser2.direct

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import java.util.Date

internal class NewMessageEventAdapter(
    private val messageAdapter: JsonAdapter<Message>,
    private val userAdapter: JsonAdapter<User>,
) : JsonAdapter<NewMessageEvent>() {

    private val streamDateFormatter = StreamDateFormatter("NewMessageEventAdapter")

    @Suppress("LongMethod")
    override fun fromJson(reader: JsonReader): NewMessageEvent? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()

        var type: String? = null
        var createdAt: Date? = null
        var rawCreatedAt: String? = null
        var user: User? = null
        var cid: String? = null
        var channelMemberCount: Int? = null
        var channelCustomName: String? = null
        var channelCustomImage: String? = null
        var channelType: String? = null
        var channelId: String? = null
        var message: Message? = null
        var watcherCount: Int = 0
        var totalUnreadCount: Int = 0
        var unreadChannels: Int = 0
        var channelMessageCount: Int? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "type" -> type = reader.nextString()
                "created_at" -> {
                    if (reader.peek() != JsonReader.Token.NULL) {
                        val rawValue = reader.nextString()
                        rawCreatedAt = rawValue
                        createdAt = streamDateFormatter.parse(rawValue)
                    } else {
                        reader.skipValue()
                    }
                }
                "user" -> user = userAdapter.fromJson(reader)
                "cid" -> cid = reader.nextString()
                "channel_member_count" -> channelMemberCount = JsonParsingUtils.readNullableInt(reader)
                "channel_custom" -> {
                    val (name, image) = parseChannelCustom(reader)
                    channelCustomName = name
                    channelCustomImage = image
                }
                "channel_type" -> channelType = reader.nextString()
                "channel_id" -> channelId = reader.nextString()
                "message" -> message = messageAdapter.fromJson(reader)
                "watcher_count" -> watcherCount = reader.nextInt()
                "total_unread_count" -> totalUnreadCount = reader.nextInt()
                "unread_channels" -> unreadChannels = reader.nextInt()
                "channel_message_count" -> channelMessageCount = JsonParsingUtils.readNullableInt(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(type, "type", reader)
        JsonParsingUtils.requireField(rawCreatedAt, "created_at", reader)
        JsonParsingUtils.requireField(user, "user", reader)
        JsonParsingUtils.requireField(cid, "cid", reader)
        JsonParsingUtils.requireField(channelType, "channel_type", reader)
        JsonParsingUtils.requireField(channelId, "channel_id", reader)
        JsonParsingUtils.requireField(message, "message", reader)

        // Enrich inline: set channelInfo + cid so parseAndProcessEvent can skip enrichIfNeeded().
        // Only copy if something actually needs to change.
        val needsChannelInfo = message.channelInfo == null
        val needsCid = message.cid != cid
        val replyTo = message.replyTo
        val needsReplyToCid = replyTo != null && replyTo.cid != cid

        val enrichedMessage = if (needsChannelInfo || needsCid || needsReplyToCid) {
            val fallbackChannelInfo = ChannelInfo(
                cid = cid,
                id = channelId,
                type = channelType,
                memberCount = channelMemberCount ?: 0,
                name = channelCustomName,
                image = channelCustomImage,
            )
            message.copy(
                channelInfo = message.channelInfo ?: fallbackChannelInfo,
                cid = if (needsCid) cid else message.cid,
                replyTo = if (needsReplyToCid) replyTo.copy(cid = cid) else replyTo,
            )
        } else {
            message
        }

        return NewMessageEvent(
            type = type,
            createdAt = createdAt ?: Date(0),
            rawCreatedAt = rawCreatedAt,
            user = user,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            message = enrichedMessage,
            watcherCount = watcherCount,
            totalUnreadCount = totalUnreadCount,
            unreadChannels = unreadChannels,
            channelMessageCount = channelMessageCount,
        )
    }

    private fun parseChannelCustom(reader: JsonReader): Pair<String?, String?> {
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
            reader.skipValue()
            return null to null
        }

        reader.beginObject()
        var name: String? = null
        var image: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "name" -> name = if (reader.peek() == JsonReader.Token.NULL) {
                    reader.nextNull()
                } else {
                    reader.nextString()
                }
                "image" -> image = if (reader.peek() == JsonReader.Token.NULL) {
                    reader.nextNull()
                } else {
                    reader.nextString()
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return name to image
    }

    override fun toJson(p0: JsonWriter, p1: NewMessageEvent?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
