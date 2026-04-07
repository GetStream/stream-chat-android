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
import io.getstream.chat.android.client.extensions.syncUnreadCountWithReads
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.ChannelTransformer
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import java.util.Date

@Suppress("LongParameterList")
internal class ChannelAdapter(
    private val messageAdapter: JsonAdapter<Message>,
    private val memberAdapter: JsonAdapter<Member>,
    private val userAdapter: JsonAdapter<User>,
    private val configAdapter: JsonAdapter<Config>,
    private val locationAdapter: JsonAdapter<Location>,
    private val dateAdapter: JsonAdapter<Date>,
    private val currentUserIdProvider: () -> UserId?,
    private val channelTransformer: ChannelTransformer,
) : JsonAdapter<Channel>() {

    @Suppress("LongMethod")
    override fun fromJson(reader: JsonReader): Channel? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var cid: String? = null
        var id: String? = null
        var type: String? = null
        var name: String? = null
        var image: String? = null
        var watcherCount: Int? = null
        var filterTags: List<String>? = null
        var frozen: Boolean? = null
        var lastMessageAt: Date? = null
        var createdAt: Date? = null
        var deletedAt: Date? = null
        var updatedAt: Date? = null
        var memberCount: Int? = null
        var messages: List<Message>? = null
        var members: List<Member>? = null
        var watchers: List<User>? = null
        var read: List<ChannelUserRead>? = null
        var config: Config? = null
        var createdBy: User? = null
        var team: String? = null
        var cooldown: Int? = null
        var pinnedMessages: List<Message>? = null
        var ownCapabilities: List<String>? = null
        var membership: Member? = null
        var activeLiveLocations: List<Location>? = null
        var messageCount: Int? = null
        var extraData: MutableMap<String, Any>? = null

        while (reader.hasNext()) {
            val key = reader.nextName()
            when (key) {
                "cid" -> cid = reader.nextString()
                "id" -> id = reader.nextString()
                "type" -> type = reader.nextString()
                "name" -> name = reader.nextString()
                "image" -> image = reader.nextString()
                "watcher_count" -> watcherCount = reader.nextInt()
                "filter_tags" -> filterTags = JsonParsingUtils.parseStringList(reader)
                "frozen" -> frozen = reader.nextBoolean()
                "last_message_at" -> lastMessageAt = dateAdapter.fromJson(reader)
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                "deleted_at" -> deletedAt = dateAdapter.fromJson(reader)
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)
                "member_count" -> memberCount = reader.nextInt()
                "messages" -> messages = JsonParsingUtils.parseList(reader, messageAdapter)
                "members" -> members = JsonParsingUtils.parseList(reader, memberAdapter)
                "watchers" -> watchers = JsonParsingUtils.parseList(reader, userAdapter)
                "read" -> read = parseChannelUserReadList(reader)
                "config" -> config = configAdapter.fromJson(reader)
                "created_by" -> createdBy = userAdapter.fromJson(reader)
                "team" -> team = reader.nextString()
                "cooldown" -> cooldown = reader.nextInt()
                "pinned_messages" -> pinnedMessages = JsonParsingUtils.parseList(reader, messageAdapter)
                "own_capabilities" -> ownCapabilities = JsonParsingUtils.parseStringList(reader)
                "membership" -> membership = memberAdapter.fromJson(reader)
                "active_live_locations" -> activeLiveLocations = JsonParsingUtils.parseList(reader, locationAdapter)
                "message_count" -> messageCount = reader.nextInt()
                else -> reader.readJsonValue()?.let { value ->
                    val map = extraData ?: mutableMapOf<String, Any>().also { extraData = it }
                    map[key] = value
                }
            }
        }
        reader.endObject()

        if (cid == null) {
            throw JsonDataException("Required value 'cid' missing at ${reader.path}")
        }
        if (id == null) {
            throw JsonDataException("Required value 'id' missing at ${reader.path}")
        }
        if (type == null) {
            throw JsonDataException("Required value 'type' missing at ${reader.path}")
        }
        if (frozen == null) {
            throw JsonDataException("Required value 'frozen' missing at ${reader.path}")
        }
        if (config == null) {
            throw JsonDataException("Required value 'config' missing at ${reader.path}")
        }

        // Build ChannelInfo to inject into messages (matching DTO path behavior)
        val channelInfo = ChannelInfo(
            cid = cid,
            id = id,
            type = type,
            memberCount = memberCount ?: 0,
            name = name,
            image = image,
        )

        // Post-process read list with correct lastReceivedEventDate (after lastMessageAt is known)
        val processedRead = read?.map { channelUserRead ->
            channelUserRead.copy(
                lastReceivedEventDate = lastMessageAt ?: channelUserRead.lastRead,
            )
        }

        return Channel(
            id = id,
            type = type,
            name = name ?: "",
            image = image ?: "",
            watcherCount = watcherCount ?: 0,
            filterTags = filterTags ?: emptyList(),
            frozen = frozen,
            createdAt = createdAt,
            deletedAt = deletedAt,
            updatedAt = updatedAt,
            memberCount = memberCount ?: 0,
            messages = messages?.map { it.copy(channelInfo = channelInfo) } ?: emptyList(),
            members = members ?: emptyList(),
            watchers = watchers ?: emptyList(),
            read = processedRead ?: emptyList(),
            config = config,
            createdBy = createdBy ?: User(),
            team = team ?: "",
            cooldown = cooldown ?: 0,
            pinnedMessages = pinnedMessages?.map { it.copy(channelInfo = channelInfo) } ?: emptyList(),
            ownCapabilities = ownCapabilities?.toSet() ?: emptySet(),
            membership = membership,
            activeLiveLocations = activeLiveLocations ?: emptyList(),
            messageCount = messageCount,
            lastMessageAt = lastMessageAt,
            extraData = extraData ?: mutableMapOf(),
        ).syncUnreadCountWithReads(currentUserIdProvider())
            .let(channelTransformer::transform)
    }

    private fun parseChannelUserReadList(reader: JsonReader): List<ChannelUserRead>? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) {
            reader.skipValue()
            return null
        }
        reader.beginArray()
        val list = mutableListOf<ChannelUserRead>()
        while (reader.hasNext()) {
            parseChannelUserRead(reader)?.let { list.add(it) }
        }
        reader.endArray()
        return list
    }

    @Suppress("ThrowsCount")
    private fun parseChannelUserRead(reader: JsonReader): ChannelUserRead? {
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

        if (user == null) {
            throw JsonDataException("Required value 'user' missing at ${reader.path}")
        }
        if (lastRead == null) {
            throw JsonDataException("Required value 'last_read' missing at ${reader.path}")
        }
        if (unreadMessages == null) {
            throw JsonDataException("Required value 'unread_messages' missing at ${reader.path}")
        }

        // Use lastRead as placeholder - will be updated with correct lastReceivedEventDate later
        return ChannelUserRead(
            user = user,
            lastReceivedEventDate = lastRead,
            lastRead = lastRead,
            unreadMessages = unreadMessages,
            lastReadMessageId = lastReadMessageId,
            lastDeliveredAt = lastDeliveredAt,
            lastDeliveredMessageId = lastDeliveredMessageId,
        )
    }

    override fun toJson(p0: JsonWriter, p1: Channel?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
