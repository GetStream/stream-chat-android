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
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.network.infrastructure.ExactDate
import io.getstream.chat.android.network.infrastructure.Serializer

/**
 * Reads a `message.new` event straight into its domain model.
 */
internal class NewMessageEventAdapter(
    private val messageAdapter: MessageAdapter,
    private val userAdapter: JsonAdapter<User>,
) : JsonAdapter<NewMessageEvent>() {

    private val exactDateAdapter by lazy { Serializer.moshi.adapter(ExactDate::class.java) }

    override fun fromJson(reader: JsonReader): NewMessageEvent? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        return read(reader)
    }

    /** Mirrors the generated path's `MessageNewEvent.toDomain`, so both build the same [NewMessageEvent]. */
    @Suppress("LongMethod", "MagicNumber")
    private fun read(reader: JsonReader): NewMessageEvent {
        reader.beginObject()

        var type: String? = null
        var createdAt: ExactDate? = null
        var user: User? = null
        var cid: String? = null
        var channelMemberCount: Int? = null
        var channelCustomName: String? = null
        var channelCustomImage: String? = null
        var message: Message? = null
        var watcherCount: Int? = null
        var totalUnreadCount: Int? = null
        var unreadChannels: Int? = null
        var channelMessageCount: Int? = null
        var groupedUnreadChannels: Map<String, Int>? = null
        var messageChannelInfo: ChannelInfo? = null

        while (reader.hasNext()) {
            when (reader.selectName(KEYS)) {
                0 -> type = reader.nextString()
                1 -> createdAt = exactDateAdapter.fromJson(reader)
                2 -> user = userAdapter.fromJson(reader)
                3 -> cid = JsonParsingUtils.readNullableString(reader)
                4 -> channelMemberCount = JsonParsingUtils.readNullableInt(reader)
                5 -> if (JsonParsingUtils.expectV2(reader, JsonReader.Token.BEGIN_OBJECT, nullable = true)) {
                    val channelCustom = JsonParsingUtils.readJsonObject(reader)
                    channelCustomName = channelCustom?.get("name") as? String
                    channelCustomImage = channelCustom?.get("image") as? String
                }
                6 -> {
                    // The backend sends the channel fields first, so the message usually takes its channel info as
                    // it is read, saving a copy of it afterwards.
                    messageChannelInfo = cid?.let {
                        channelInfo(it, channelMemberCount, channelCustomName, channelCustomImage)
                    }
                    message = messageAdapter.fromJson(reader, messageChannelInfo)
                }
                7 -> watcherCount = JsonParsingUtils.readNullableInt(reader)
                8 -> totalUnreadCount = JsonParsingUtils.readNullableInt(reader)
                9 -> unreadChannels = JsonParsingUtils.readNullableInt(reader)
                10 -> channelMessageCount = JsonParsingUtils.readNullableInt(reader)
                11 -> if (JsonParsingUtils.expectV2(reader, JsonReader.Token.BEGIN_OBJECT, nullable = true)) {
                    groupedUnreadChannels = JsonParsingUtils.parseIntMap(reader)
                }
                // Not mapped, but the generated model rejects a null or a non-object.
                12 -> if (JsonParsingUtils.expectV2(reader, JsonReader.Token.BEGIN_OBJECT, nullable = false)) {
                    reader.skipValue()
                }
                else -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(createdAt, "createdAt", reader)
        JsonParsingUtils.requireField(message, "message", reader)
        JsonParsingUtils.requireField(type, "type", reader)
        JsonParsingUtils.requireField(cid, "cid", reader)
        JsonParsingUtils.requireField(user, "user", reader)
        // The message carries no channel, so it and its quoted messages took the event's channel info as they were
        // read. A channel field after the message would change it once the message transformer has run, so such a
        // frame, which the backend doesn't send, is left to the generated path.
        val channelInfo = messageChannelInfo?.takeIf {
            it.cid == cid && it.memberCount == (channelMemberCount ?: 0) &&
                it.name == channelCustomName && it.image == channelCustomImage
        } ?: throw JsonDataException("A channel field follows the message at ${reader.path}")
        val channelType = requireNotNull(channelInfo.type)
        val channelId = requireNotNull(channelInfo.id)

        return NewMessageEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = user,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            message = message,
            watcherCount = watcherCount ?: 0,
            totalUnreadCount = totalUnreadCount ?: 0,
            unreadChannels = unreadChannels ?: 0,
            channelMessageCount = channelMessageCount,
            groupedUnreadChannels = groupedUnreadChannels,
        )
    }

    private fun channelInfo(cid: String, memberCount: Int?, name: String?, image: String?): ChannelInfo {
        val (channelType, channelId) = cid.cidToTypeAndId()
        return ChannelInfo(
            cid = cid,
            id = channelId,
            type = channelType,
            memberCount = memberCount ?: 0,
            name = name,
            image = image,
        )
    }

    private companion object {
        /** The keys, matched without allocating a key string, as the generated adapters match them. */
        private val KEYS = JsonReader.Options.of(
            "type",
            "created_at",
            "user",
            "cid",
            "channel_member_count",
            "channel_custom",
            "message",
            "watcher_count",
            "total_unread_count",
            "unread_channels",
            "channel_message_count",
            "grouped_unread_channels",
            "custom",
        )
    }

    override fun toJson(p0: JsonWriter, p1: NewMessageEvent?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
