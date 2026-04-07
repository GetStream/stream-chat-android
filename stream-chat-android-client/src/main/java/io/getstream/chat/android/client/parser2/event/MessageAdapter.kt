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
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageModerationDetails
import io.getstream.chat.android.models.MessageReminderInfo
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.Moderation
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.models.User
import java.util.Date

@Suppress("LongParameterList")
internal class MessageAdapter(
    private val attachmentAdapter: JsonAdapter<Attachment>,
    private val channelInfoAdapter: JsonAdapter<ChannelInfo>,
    private val reactionAdapter: JsonAdapter<Reaction>,
    private val reactionGroupAdapter: ReactionGroupAdapter,
    private val userAdapter: JsonAdapter<User>,
    private val moderationDetailsAdapter: JsonAdapter<MessageModerationDetails>,
    private val moderationAdapter: JsonAdapter<Moderation>,
    private val pollAdapter: JsonAdapter<Poll>,
    private val reminderAdapter: JsonAdapter<MessageReminderInfo>,
    private val locationAdapter: JsonAdapter<Location>,
    private val dateAdapter: JsonAdapter<Date>,
    private val messageTransformer: MessageTransformer,
) : JsonAdapter<Message>() {

    override fun fromJson(reader: JsonReader): Message? {
        return fromJson(reader, fallbackChannelInfo = null)
    }

    @Suppress("LongMethod", "ThrowsCount")
    fun fromJson(reader: JsonReader, fallbackChannelInfo: ChannelInfo?): Message? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()

        var attachments: List<Attachment>? = null
        var channel: ChannelInfo? = null
        var cid: String? = null
        var command: String? = null
        var createdAt: Date? = null
        var deletedAt: Date? = null
        var html: String? = null
        var i18n: Map<String, String>? = null
        var id: String? = null
        var latestReactions: List<Reaction>? = null
        var mentionedUsers: List<User>? = null
        var ownReactions: List<Reaction>? = null
        var parentId: String? = null
        var pinExpires: Date? = null
        var pinned: Boolean? = null
        var pinnedAt: Date? = null
        var messageTextUpdatedAt: Date? = null
        var pinnedBy: User? = null
        var quotedMessage: Message? = null
        var quotedMessageId: String? = null
        var reactionCounts: Map<String, Int>? = null
        var reactionScores: Map<String, Int>? = null
        var reactionGroups: Map<String, ReactionGroup>? = null
        var replyCount: Int? = null
        var deletedReplyCount: Int? = null
        var shadowed: Boolean? = null
        var showInChannel: Boolean? = null
        var silent: Boolean? = null
        var text: String? = null
        var threadParticipants: List<User>? = null
        var type: String? = null
        var updatedAt: Date? = null
        var user: User? = null
        var moderationDetails: MessageModerationDetails? = null
        var moderation: Moderation? = null
        var poll: Poll? = null
        var reminder: MessageReminderInfo? = null
        var sharedLocation: Location? = null
        var channelRole: String? = null
        var deletedForMe: Boolean? = null
        var extraData: MutableMap<String, Any>? = null

        while (reader.hasNext()) {
            val key = reader.nextName()
            when (key) {
                "attachments" -> attachments = parseAttachmentsList(reader)
                "channel" -> channel = channelInfoAdapter.fromJson(reader)
                "cid" -> cid = reader.nextString()
                "command" -> command = reader.nextString()
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                "deleted_at" -> deletedAt = dateAdapter.fromJson(reader)
                "html" -> html = reader.nextString()
                "i18n" -> i18n = parseStringMap(reader)
                "id" -> id = reader.nextString()
                "latest_reactions" -> latestReactions = parseReactionsList(reader)
                "mentioned_users" -> mentionedUsers = parseUsersList(reader)
                "own_reactions" -> ownReactions = parseReactionsList(reader)
                "parent_id" -> parentId = reader.nextString()
                "pin_expires" -> pinExpires = dateAdapter.fromJson(reader)
                "pinned" -> pinned = reader.nextBoolean()
                "pinned_at" -> pinnedAt = dateAdapter.fromJson(reader)
                "message_text_updated_at" -> messageTextUpdatedAt = dateAdapter.fromJson(reader)
                "pinned_by" -> pinnedBy = userAdapter.fromJson(reader)
                "quoted_message" -> {
                    // Recursive parsing: pass the fallback channel info along
                    val resolvedChannelInfo = channel ?: fallbackChannelInfo
                    quotedMessage = fromJson(reader, resolvedChannelInfo)
                }

                "quoted_message_id" -> quotedMessageId = reader.nextString()
                "reaction_counts" -> reactionCounts = parseIntMap(reader)
                "reaction_scores" -> reactionScores = parseIntMap(reader)
                "reaction_groups" -> reactionGroups = reactionGroupAdapter.parseReactionGroupsMap(reader)
                "reply_count" -> replyCount = reader.nextInt()
                "deleted_reply_count" -> deletedReplyCount = reader.nextInt()
                "shadowed" -> shadowed = reader.nextBoolean()
                "show_in_channel" -> showInChannel = reader.nextBoolean()
                "silent" -> silent = reader.nextBoolean()
                "text" -> text = reader.nextString()
                "thread_participants" -> threadParticipants = parseUsersList(reader)
                "type" -> type = reader.nextString()
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)
                "user" -> user = userAdapter.fromJson(reader)
                "moderation_details" -> moderationDetails = moderationDetailsAdapter.fromJson(reader)
                "moderation" -> moderation = moderationAdapter.fromJson(reader)
                "poll" -> poll = pollAdapter.fromJson(reader)
                "reminder" -> reminder = reminderAdapter.fromJson(reader)
                "shared_location" -> sharedLocation = locationAdapter.fromJson(reader)
                "member" -> channelRole = parseMemberChannelRole(reader)
                "deleted_for_me" -> deletedForMe = reader.nextBoolean()
                else -> reader.readJsonValue()?.let { value ->
                    val map = extraData ?: mutableMapOf<String, Any>().also { extraData = it }
                    map[key] = value
                }
            }
        }
        reader.endObject()

        if (attachments == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'attachments' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (cid == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'cid' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (createdAt == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'created_at' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (html == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'html' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (id == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'id' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (latestReactions == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'latest_reactions' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (mentionedUsers == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'mentioned_users' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (ownReactions == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'own_reactions' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (replyCount == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'reply_count' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (deletedReplyCount == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'deleted_reply_count' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (silent == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'silent' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (text == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'text' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (type == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'type' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (updatedAt == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'updated_at' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (user == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'user' missing at ${reader.path} at ${reader.path}",
            )
        }

        val resolvedChannelInfo = channel ?: fallbackChannelInfo

        // Filter reactions by messageId (matching DomainMapping behavior)
        val filteredLatestReactions = latestReactions.filter { it.messageId == id }
        val filteredOwnReactions = ownReactions.filter { it.messageId == id }

        // Calculate last update time: max of updated_at and poll?.updatedAt
        val lastUpdateTime = listOfNotNull(
            updatedAt,
            poll?.updatedAt,
        ).maxByOrNull { it.time } ?: updatedAt

        return Message(
            attachments = attachments,
            channelInfo = resolvedChannelInfo,
            cid = cid,
            command = command,
            createdAt = createdAt,
            deletedAt = deletedAt,
            html = html,
            i18n = i18n ?: emptyMap(),
            id = id,
            latestReactions = filteredLatestReactions,
            mentionedUsersIds = mentionedUsers.map { it.id },
            mentionedUsers = mentionedUsers,
            ownReactions = filteredOwnReactions,
            parentId = parentId,
            pinExpires = pinExpires,
            pinned = pinned ?: false,
            pinnedAt = pinnedAt,
            pinnedBy = pinnedBy,
            reactionCounts = reactionCounts ?: mutableMapOf(),
            reactionScores = reactionScores ?: mutableMapOf(),
            reactionGroups = reactionGroups ?: emptyMap(),
            replyCount = replyCount,
            deletedReplyCount = deletedReplyCount,
            replyMessageId = quotedMessageId,
            replyTo = quotedMessage,
            shadowed = shadowed ?: false,
            showInChannel = showInChannel ?: false,
            silent = silent,
            text = text,
            threadParticipants = threadParticipants ?: emptyList(),
            type = type,
            updatedAt = lastUpdateTime,
            user = user,
            moderationDetails = moderationDetails,
            moderation = moderation,
            messageTextUpdatedAt = messageTextUpdatedAt,
            poll = poll,
            restrictedVisibility = emptyList(),
            reminder = reminder,
            sharedLocation = sharedLocation,
            channelRole = channelRole,
            deletedForMe = deletedForMe ?: false,
            extraData = extraData ?: emptyMap(),
        ).let(messageTransformer::transform)
    }

    private fun parseAttachmentsList(reader: JsonReader): List<Attachment>? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) {
            reader.skipValue()
            return null
        }
        reader.beginArray()
        val list = mutableListOf<Attachment>()
        while (reader.hasNext()) {
            attachmentAdapter.fromJson(reader)?.let { list.add(it) }
        }
        reader.endArray()
        return list
    }

    private fun parseReactionsList(reader: JsonReader): List<Reaction>? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) {
            reader.skipValue()
            return null
        }
        reader.beginArray()
        val list = mutableListOf<Reaction>()
        while (reader.hasNext()) {
            reactionAdapter.fromJson(reader)?.let { list.add(it) }
        }
        reader.endArray()
        return list
    }

    private fun parseUsersList(reader: JsonReader): List<User>? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) {
            reader.skipValue()
            return null
        }
        reader.beginArray()
        val list = mutableListOf<User>()
        while (reader.hasNext()) {
            userAdapter.fromJson(reader)?.let { list.add(it) }
        }
        reader.endArray()
        return list
    }

    private fun parseIntMap(reader: JsonReader): Map<String, Int>? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
            reader.skipValue()
            return null
        }
        reader.beginObject()
        val map = mutableMapOf<String, Int>()
        while (reader.hasNext()) {
            val key = reader.nextName()
            map[key] = reader.nextInt()
        }
        reader.endObject()
        return map
    }

    private fun parseStringMap(reader: JsonReader): Map<String, String>? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
            reader.skipValue()
            return null
        }
        reader.beginObject()
        val map = mutableMapOf<String, String>()
        while (reader.hasNext()) {
            val key = reader.nextName()
            map[key] = reader.nextString()
        }
        reader.endObject()
        return map
    }

    private fun parseMemberChannelRole(reader: JsonReader): String? {
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
            reader.skipValue()
            return null
        }

        reader.beginObject()
        var channelRole: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "channel_role" -> channelRole = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return channelRole
    }

    override fun toJson(p0: JsonWriter, p1: Message?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
