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
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.network.infrastructure.ExactDate
import io.getstream.chat.android.network.infrastructure.Serializer
import java.util.Date

/**
 * Reads a `message.new` event straight into its domain model, in the v1 shape or, with [v2], the v2 one. [userAdapter]
 * and [messageAdapter] must read the same shape.
 */
internal class NewMessageEventAdapter(
    private val messageAdapter: MessageAdapter,
    private val userAdapter: JsonAdapter<User>,
    private val v2: Boolean = false,
) : JsonAdapter<NewMessageEvent>() {

    private val streamDateFormatter = StreamDateFormatter("NewMessageEventAdapter")
    private val exactDateAdapter by lazy { Serializer.moshi.adapter(ExactDate::class.java) }

    @Suppress("LongMethod")
    override fun fromJson(reader: JsonReader): NewMessageEvent? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (v2) return readV2(reader)

        reader.beginObject()

        var type: String? = null
        var createdAt: Date? = null
        var rawCreatedAt: String? = null
        var user: User? = null
        var cid: String? = null
        var channelMemberCount: Int? = null
        var channelCustomName: String? = null
        var channelCustomImage: String? = null
        var message: Message? = null
        var watcherCount: Int = 0
        var totalUnreadCount: Int = 0
        var unreadChannels: Int = 0
        var channelMessageCount: Int? = null
        var groupedUnreadChannels: Map<String, Int>? = null

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
                "message" -> message = messageAdapter.fromJson(reader)
                "watcher_count" -> watcherCount = JsonParsingUtils.readNullableInt(reader) ?: 0
                "total_unread_count" -> totalUnreadCount = JsonParsingUtils.readNullableInt(reader) ?: 0
                "unread_channels" -> unreadChannels = JsonParsingUtils.readNullableInt(reader) ?: 0
                "channel_message_count" -> channelMessageCount = JsonParsingUtils.readNullableInt(reader)
                "grouped_unread_channels" -> groupedUnreadChannels = JsonParsingUtils.parseIntMap(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(type, "type", reader)
        JsonParsingUtils.requireField(rawCreatedAt, "created_at", reader)
        if (createdAt == null) {
            // rawCreatedAt was present but unparseable; mirror DTO path which throws via ExactDateAdapter.
            throw JsonDataException("Unparseable 'created_at' value '$rawCreatedAt' at ${reader.path}")
        }
        JsonParsingUtils.requireField(user, "user", reader)
        JsonParsingUtils.requireField(cid, "cid", reader)
        JsonParsingUtils.requireField(message, "message", reader)
        val (channelType, channelId) = cid.cidToTypeAndId()

        // Enrich inline: set channelInfo + cid so parseAndProcessEvent can skip enrichIfNeeded().
        // Only copy if something actually needs to change.
        val needsChannelInfo = message.channelInfo == null
        val needsCid = message.cid != cid
        val replyTo = message.replyTo
        val needsReplyToCid = replyTo != null && replyTo.cid != cid
        val needsReplyToChannelInfo = replyTo != null && replyTo.channelInfo == null
        val needsReplyToEnrichment = needsReplyToCid || needsReplyToChannelInfo

        val enrichedMessage = if (needsChannelInfo || needsCid || needsReplyToEnrichment) {
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
                replyTo = replyTo?.let { rt ->
                    if (needsReplyToEnrichment) {
                        rt.copy(
                            cid = if (needsReplyToCid) cid else rt.cid,
                            channelInfo = rt.channelInfo ?: fallbackChannelInfo,
                        )
                    } else {
                        rt
                    }
                },
            )
        } else {
            message
        }

        return NewMessageEvent(
            type = type,
            createdAt = createdAt,
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
            groupedUnreadChannels = groupedUnreadChannels,
        )
    }

    /** Mirrors the generated path's `MessageNewEvent.toDomain`, so both build the same [NewMessageEvent]. */
    @Suppress("LongMethod", "MagicNumber")
    private fun readV2(reader: JsonReader): NewMessageEvent {
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
            when (reader.selectName(V2_KEYS)) {
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
                // Not mapped, but the generated model rejects a non-object.
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

    private companion object {
        /** The v2 keys, matched without allocating a key string, as the generated adapters match them. */
        private val V2_KEYS = JsonReader.Options.of(
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
