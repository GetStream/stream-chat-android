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
import io.getstream.chat.android.models.UserGroup
import java.util.Date

@Suppress("LongParameterList")
internal class MessageAdapter(
    private val attachmentAdapter: JsonAdapter<Attachment>,
    private val channelInfoAdapter: JsonAdapter<ChannelInfo>,
    private val reactionAdapter: JsonAdapter<Reaction>,
    private val reactionGroupAdapter: ReactionGroupAdapter,
    private val userAdapter: JsonAdapter<User>,
    private val userGroupAdapter: JsonAdapter<UserGroup>,
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
        var mentionedHere: Boolean? = null
        var mentionedChannel: Boolean? = null
        var mentionedGroups: List<UserGroup>? = null
        var mentionedRoles: List<String>? = null
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
                "attachments" -> attachments = JsonParsingUtils.parseList(reader, attachmentAdapter)
                "channel" -> channel = channelInfoAdapter.fromJson(reader)
                "cid" -> cid = reader.nextString()
                "command" -> command = JsonParsingUtils.readNullableString(reader)
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                "deleted_at" -> deletedAt = dateAdapter.fromJson(reader)
                "html" -> html = reader.nextString()
                // i18n is non-nullable in DownstreamMessageDto (with default), so the DTO path
                // throws on explicit JSON null even though missing is fine. Match that here.
                "i18n" -> {
                    JsonParsingUtils.rejectExplicitNull(reader, "i18n")
                    i18n = JsonParsingUtils.parseStringMap(reader)
                }
                "id" -> id = reader.nextString()
                "latest_reactions" -> latestReactions = JsonParsingUtils.parseList(reader, reactionAdapter)
                "mentioned_users" -> mentionedUsers = JsonParsingUtils.parseList(reader, userAdapter)
                "mentioned_here" -> mentionedHere = JsonParsingUtils.readNullableBoolean(reader)
                "mentioned_channel" -> mentionedChannel = JsonParsingUtils.readNullableBoolean(reader)
                "mentioned_groups" -> mentionedGroups = JsonParsingUtils.parseList(reader, userGroupAdapter)
                "mentioned_roles" -> mentionedRoles = JsonParsingUtils.parseStringList(reader)
                "own_reactions" -> ownReactions = JsonParsingUtils.parseList(reader, reactionAdapter)
                "parent_id" -> parentId = JsonParsingUtils.readNullableString(reader)
                "pin_expires" -> pinExpires = dateAdapter.fromJson(reader)
                "pinned" -> pinned = reader.nextBoolean()
                "pinned_at" -> pinnedAt = dateAdapter.fromJson(reader)
                "message_text_updated_at" -> messageTextUpdatedAt = dateAdapter.fromJson(reader)
                "pinned_by" -> pinnedBy = userAdapter.fromJson(reader)
                "quoted_message" -> {
                    // Parse with outer fallback only; parent's channel may not be parsed yet.
                    // Post-loop enrichment ensures parity with DTO toDomain(channelInfo).
                    quotedMessage = fromJson(reader, fallbackChannelInfo)
                }

                "quoted_message_id" -> quotedMessageId = JsonParsingUtils.readNullableString(reader)
                "reaction_counts" -> reactionCounts = JsonParsingUtils.parseIntMap(reader)
                "reaction_scores" -> reactionScores = JsonParsingUtils.parseIntMap(reader)
                "reaction_groups" -> reactionGroups = reactionGroupAdapter.parseReactionGroupsMap(reader)
                "reply_count" -> replyCount = reader.nextInt()
                "deleted_reply_count" -> deletedReplyCount = reader.nextInt()
                "shadowed" -> shadowed = reader.nextBoolean()
                "show_in_channel" -> showInChannel = reader.nextBoolean()
                "silent" -> silent = reader.nextBoolean()
                "text" -> text = reader.nextString()
                // thread_participants is non-nullable in DownstreamMessageDto (with default),
                // so the DTO path throws on explicit JSON null. Match that here.
                "thread_participants" -> {
                    JsonParsingUtils.rejectExplicitNull(reader, "thread_participants")
                    threadParticipants = JsonParsingUtils.parseList(reader, userAdapter)
                }
                "type" -> type = reader.nextString()
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)
                "user" -> user = userAdapter.fromJson(reader)
                "moderation_details" -> moderationDetails = moderationDetailsAdapter.fromJson(reader)
                "moderation" -> moderation = moderationAdapter.fromJson(reader)
                "poll" -> poll = pollAdapter.fromJson(reader)
                "reminder" -> reminder = reminderAdapter.fromJson(reader)
                "shared_location" -> sharedLocation = locationAdapter.fromJson(reader)
                "member" -> channelRole = parseMemberChannelRole(reader)
                "deleted_for_me" -> deletedForMe = JsonParsingUtils.readNullableBoolean(reader)
                else -> extraData = JsonParsingUtils.accumulateExtraData(key, reader, extraData)
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(attachments, "attachments", reader)
        JsonParsingUtils.requireField(cid, "cid", reader)
        JsonParsingUtils.requireField(createdAt, "created_at", reader)
        JsonParsingUtils.requireField(html, "html", reader)
        JsonParsingUtils.requireField(id, "id", reader)
        JsonParsingUtils.requireField(latestReactions, "latest_reactions", reader)
        JsonParsingUtils.requireField(mentionedUsers, "mentioned_users", reader)
        JsonParsingUtils.requireField(ownReactions, "own_reactions", reader)
        JsonParsingUtils.requireField(replyCount, "reply_count", reader)
        JsonParsingUtils.requireField(deletedReplyCount, "deleted_reply_count", reader)
        JsonParsingUtils.requireField(silent, "silent", reader)
        JsonParsingUtils.requireField(text, "text", reader)
        JsonParsingUtils.requireField(type, "type", reader)
        JsonParsingUtils.requireField(updatedAt, "updated_at", reader)
        JsonParsingUtils.requireField(user, "user", reader)

        val resolvedChannelInfo = channel ?: fallbackChannelInfo

        // Enrich the quoted message with the parent's resolved channelInfo (matching DTO
        // toDomain behavior where the parent passes its resolved channelInfo as fallback to
        // quoted_message.toDomain()).
        //
        // Known limit: channelInfo propagation is only one level deep. A two-deep chain
        // (message -> quoted_message -> quoted_message) where the inner two messages have no
        // `channel` field will leave the innermost message's channelInfo null, while the DTO
        // path would propagate the outer message's channelInfo down two levels. Two-deep
        // quoted_message chains are rare in practice; if support is needed, this fallback
        // needs to be threaded recursively (or replaced with a post-hoc traversal).
        val enrichedQuotedMessage = quotedMessage?.let { qm ->
            if (resolvedChannelInfo != null && qm.channelInfo == null) {
                qm.copy(channelInfo = resolvedChannelInfo)
            } else {
                qm
            }
        }

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
            mentionedUsers = mentionedUsers,
            mentionedHere = mentionedHere == true,
            mentionedChannel = mentionedChannel == true,
            mentionedGroups = mentionedGroups.orEmpty(),
            mentionedRoles = mentionedRoles.orEmpty(),
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
            replyTo = enrichedQuotedMessage,
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

    private fun parseMemberChannelRole(reader: JsonReader): String? {
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
            reader.skipValue()
            return null
        }

        reader.beginObject()
        var channelRole: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "channel_role" -> channelRole = JsonParsingUtils.readNullableString(reader)
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
