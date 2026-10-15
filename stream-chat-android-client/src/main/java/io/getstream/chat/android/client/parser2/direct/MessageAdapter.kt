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
import com.squareup.moshi.Types
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.MemberInfo
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
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.ChannelMemberPartialResponse
import io.getstream.chat.android.network.models.DraftResponse
import io.getstream.chat.android.network.models.ModerationV2Response
import io.getstream.chat.android.network.models.PollResponseData
import io.getstream.chat.android.network.models.ReminderResponseData
import io.getstream.chat.android.network.models.SharedLocationResponseData
import io.getstream.chat.android.network.models.UserGroupResponse
import java.util.Date

/**
 * Reads a message straight into its domain model. Without [v2Mapping] it reads the v1 shape, custom data at the root.
 * With it, the v2 shape, mapped as the generated v2 path maps it: custom data under `custom`, and the nested objects
 * messages rarely carry read through their generated models.
 */
@Suppress("LongParameterList", "LargeClass")
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
    private val v2Mapping: DomainMapping? = null,
) : JsonAdapter<Message>() {

    private val draftAdapter by lazy { Serializer.moshi.adapter(DraftResponse::class.java) }
    private val memberAdapter by lazy { Serializer.moshi.adapter(ChannelMemberPartialResponse::class.java) }
    private val mentionedChannelMembersAdapter by lazy {
        Serializer.moshi.adapter<Map<String, ChannelMemberPartialResponse?>>(
            Types.newParameterizedType(Map::class.java, String::class.java, ChannelMemberPartialResponse::class.java),
        )
    }
    private val mentionedGroupsAdapter by lazy {
        Serializer.moshi.adapter<List<UserGroupResponse>>(
            Types.newParameterizedType(List::class.java, UserGroupResponse::class.java),
        )
    }
    private val imageLabelsAdapter by lazy {
        Serializer.moshi.adapter<Map<String, List<String>>>(
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Types.newParameterizedType(List::class.java, String::class.java),
            ),
        )
    }
    private val v2ModerationAdapter by lazy { Serializer.moshi.adapter(ModerationV2Response::class.java) }
    private val v2PollAdapter by lazy { Serializer.moshi.adapter(PollResponseData::class.java) }
    private val v2ReminderAdapter by lazy { Serializer.moshi.adapter(ReminderResponseData::class.java) }
    private val v2LocationAdapter by lazy { Serializer.moshi.adapter(SharedLocationResponseData::class.java) }

    override fun fromJson(reader: JsonReader): Message? {
        return fromJson(reader, fallbackChannelInfo = null)
    }

    @Suppress("LongMethod", "ThrowsCount", "DEPRECATION")
    fun fromJson(reader: JsonReader, fallbackChannelInfo: ChannelInfo?): Message? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (v2Mapping != null) return readV2(reader, fallbackChannelInfo, v2Mapping)

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
        var member: MemberInfo? = null
        var mentionedChannelMembers: Map<String, MemberInfo>? = null
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
                "i18n" -> i18n = JsonParsingUtils.parseStringMap(reader)
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
                "show_in_channel" -> showInChannel = JsonParsingUtils.readNullableBoolean(reader)
                "silent" -> silent = reader.nextBoolean()
                "text" -> text = reader.nextString()
                "thread_participants" -> threadParticipants = JsonParsingUtils.parseList(reader, userAdapter)
                "type" -> type = reader.nextString()
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)
                "user" -> user = userAdapter.fromJson(reader)
                "moderation_details" -> moderationDetails = moderationDetailsAdapter.fromJson(reader)
                "moderation" -> moderation = moderationAdapter.fromJson(reader)
                "poll" -> poll = pollAdapter.fromJson(reader)
                "reminder" -> reminder = reminderAdapter.fromJson(reader)
                "shared_location" -> sharedLocation = locationAdapter.fromJson(reader)
                "member" -> member = parseMemberInfo(reader)
                "mentioned_channel_members" -> mentionedChannelMembers = parseMentionedChannelMembers(reader)
                "deleted_for_me" -> deletedForMe = JsonParsingUtils.readNullableBoolean(reader)
                else -> extraData = JsonParsingUtils.accumulateExtraData(key, reader, extraData)
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(cid, "cid", reader)
        JsonParsingUtils.requireField(createdAt, "created_at", reader)
        JsonParsingUtils.requireField(html, "html", reader)
        JsonParsingUtils.requireField(id, "id", reader)
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
        val filteredLatestReactions = latestReactions.orEmpty().filter { it.messageId == id }
        val filteredOwnReactions = ownReactions.orEmpty().filter { it.messageId == id }

        // Calculate last update time: max of updated_at and poll?.updatedAt
        val lastUpdateTime = listOfNotNull(
            updatedAt,
            poll?.updatedAt,
        ).maxByOrNull { it.time } ?: updatedAt

        return Message(
            attachments = attachments.orEmpty(),
            channelInfo = resolvedChannelInfo,
            cid = cid,
            command = command,
            createdAt = createdAt,
            deletedAt = deletedAt,
            html = html,
            i18n = i18n ?: emptyMap(),
            id = id,
            latestReactions = filteredLatestReactions,
            mentionedUsers = mentionedUsers.orEmpty(),
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
            channelRole = member?.channelRole,
            member = member,
            mentionedChannelMembers = mentionedChannelMembers.orEmpty(),
            deletedForMe = deletedForMe ?: false,
            extraData = extraData ?: emptyMap(),
        ).let(messageTransformer::transform)
    }

    /** Mirrors the generated path's `MessageResponse.toDomain`, so both build the same [Message]. */
    @Suppress("LongMethod", "CyclomaticComplexMethod", "ComplexMethod", "MagicNumber")
    private fun readV2(reader: JsonReader, fallbackChannelInfo: ChannelInfo?, mapping: DomainMapping): Message {
        reader.beginObject()

        var attachments: List<Attachment>? = null
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
        var mentionedGroups: List<UserGroupResponse>? = null
        var mentionedGroupIds: List<String>? = null
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
        var moderation: ModerationV2Response? = null
        var poll: PollResponseData? = null
        var reminder: ReminderResponseData? = null
        var sharedLocation: SharedLocationResponseData? = null
        var member: ChannelMemberPartialResponse? = null
        var mentionedChannelMembers: Map<String, ChannelMemberPartialResponse?>? = null
        var deletedForMe: Boolean? = null
        var draft: DraftResponse? = null
        var imageLabels: Map<String, List<String>>? = null
        var mml: String? = null
        var pollId: String? = null
        var restrictedVisibility: List<String>? = null
        var custom: Map<String, Any?>? = null

        while (reader.hasNext()) {
            when (reader.selectName(V2_KEYS)) {
                0 -> attachments = JsonParsingUtils.parseList(reader, attachmentAdapter)
                1 -> cid = reader.nextString()
                2 -> command = JsonParsingUtils.readNullableString(reader)
                3 -> createdAt = dateAdapter.fromJson(reader)
                4 -> deletedAt = dateAdapter.fromJson(reader)
                5 -> html = reader.nextString()
                6 -> i18n = JsonParsingUtils.parseStringMap(reader)
                7 -> id = reader.nextString()
                8 -> latestReactions = JsonParsingUtils.parseList(reader, reactionAdapter)
                9 -> mentionedUsers = JsonParsingUtils.parseList(reader, userAdapter)
                10 -> mentionedHere = reader.nextBoolean()
                11 -> mentionedChannel = reader.nextBoolean()
                12 -> mentionedGroups = mentionedGroupsAdapter.fromJson(reader)
                13 -> mentionedGroupIds = JsonParsingUtils.parseStringList(reader)
                14 -> mentionedRoles = JsonParsingUtils.parseStringList(reader)
                15 -> ownReactions = JsonParsingUtils.parseList(reader, reactionAdapter)
                16 -> parentId = JsonParsingUtils.readNullableString(reader)
                17 -> pinExpires = dateAdapter.fromJson(reader)
                18 -> pinned = reader.nextBoolean()
                19 -> pinnedAt = dateAdapter.fromJson(reader)
                20 -> messageTextUpdatedAt = dateAdapter.fromJson(reader)
                21 -> pinnedBy = userAdapter.fromJson(reader)
                22 -> quotedMessage = fromJson(reader, fallbackChannelInfo)
                23 -> quotedMessageId = JsonParsingUtils.readNullableString(reader)
                24 -> reactionCounts = JsonParsingUtils.parseIntMap(reader)
                25 -> reactionScores = JsonParsingUtils.parseIntMap(reader)
                26 -> reactionGroups = reactionGroupAdapter.parseReactionGroupsMap(reader)
                27 -> replyCount = reader.nextInt()
                28 -> deletedReplyCount = reader.nextInt()
                29 -> shadowed = reader.nextBoolean()
                30 -> showInChannel = JsonParsingUtils.readNullableBoolean(reader)
                31 -> silent = reader.nextBoolean()
                32 -> text = reader.nextString()
                33 -> threadParticipants = JsonParsingUtils.parseList(reader, userAdapter)
                34 -> type = reader.nextString()
                35 -> updatedAt = dateAdapter.fromJson(reader)
                36 -> user = userAdapter.fromJson(reader)
                37 -> moderation = v2ModerationAdapter.fromJson(reader)
                38 -> poll = v2PollAdapter.fromJson(reader)
                39 -> reminder = v2ReminderAdapter.fromJson(reader)
                40 -> sharedLocation = v2LocationAdapter.fromJson(reader)
                41 -> member = memberAdapter.fromJson(reader)
                42 -> mentionedChannelMembers = mentionedChannelMembersAdapter.fromJson(reader)
                43 -> deletedForMe = JsonParsingUtils.readNullableBoolean(reader)
                44 -> draft = draftAdapter.fromJson(reader)
                45 -> imageLabels = imageLabelsAdapter.fromJson(reader)
                46 -> mml = JsonParsingUtils.readNullableString(reader)
                47 -> pollId = JsonParsingUtils.readNullableString(reader)
                48 -> restrictedVisibility = JsonParsingUtils.parseStringList(reader)
                49 -> custom = JsonParsingUtils.readJsonObject(reader)
                else -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(cid, "cid", reader)
        JsonParsingUtils.requireField(createdAt, "createdAt", reader)
        JsonParsingUtils.requireField(deletedReplyCount, "deletedReplyCount", reader)
        JsonParsingUtils.requireField(html, "html", reader)
        JsonParsingUtils.requireField(id, "id", reader)
        JsonParsingUtils.requireField(mentionedChannel, "mentionedChannel", reader)
        JsonParsingUtils.requireField(mentionedHere, "mentionedHere", reader)
        JsonParsingUtils.requireField(pinned, "pinned", reader)
        JsonParsingUtils.requireField(replyCount, "replyCount", reader)
        JsonParsingUtils.requireField(shadowed, "shadowed", reader)
        JsonParsingUtils.requireField(silent, "silent", reader)
        JsonParsingUtils.requireField(text, "text", reader)
        JsonParsingUtils.requireField(type, "type", reader)
        JsonParsingUtils.requireField(updatedAt, "updatedAt", reader)
        JsonParsingUtils.requireField(user, "user", reader)

        val customData = custom.orEmpty()
        return with(mapping) {
            Message(
                attachments = attachments.orEmpty(),
                channelInfo = fallbackChannelInfo,
                cid = cid,
                command = command,
                createdAt = createdAt,
                deletedAt = deletedAt,
                html = html,
                i18n = i18n.orEmpty(),
                id = id,
                latestReactions = latestReactions.orEmpty().filter { it.messageId == id },
                mentionedUsers = mentionedUsers.orEmpty(),
                mentionedHere = mentionedHere,
                mentionedChannel = mentionedChannel,
                mentionedGroups = mentionedGroups.orEmpty().map { it.toDomain() },
                mentionedRoles = mentionedRoles.orEmpty(),
                ownReactions = ownReactions.orEmpty().filter { it.messageId == id },
                parentId = parentId,
                pinExpires = pinExpires,
                pinned = pinned,
                pinnedAt = pinnedAt,
                pinnedBy = pinnedBy,
                reactionCounts = reactionCounts.orEmpty().toMutableMap(),
                reactionScores = reactionScores.orEmpty().toMutableMap(),
                reactionGroups = reactionGroups.orEmpty(),
                replyCount = replyCount,
                deletedReplyCount = deletedReplyCount,
                replyMessageId = quotedMessageId,
                replyTo = quotedMessage,
                shadowed = shadowed,
                showInChannel = showInChannel ?: false,
                silent = silent,
                text = text,
                threadParticipants = threadParticipants.orEmpty(),
                type = type,
                updatedAt = listOfNotNull(updatedAt, poll?.updatedAt).maxBy { it.time },
                user = user,
                moderationDetails = messageModerationDetails(customData),
                moderation = moderation?.toDomain(),
                messageTextUpdatedAt = messageTextUpdatedAt,
                poll = poll?.toDomain(),
                restrictedVisibility = emptyList(),
                reminder = reminder?.toReminderInfoDomain(),
                sharedLocation = sharedLocation?.toDomain(),
                channelRole = member?.channelRole,
                member = member?.toDomain(),
                mentionedChannelMembers = mentionedChannelMembers.orEmpty()
                    .mapNotNull { (userId, memberInfo) -> memberInfo?.let { userId to it.toDomain() } }
                    .toMap(),
                deletedForMe = deletedForMe ?: false,
                extraData = messageExtraData(
                    custom = customData,
                    draft = draft,
                    imageLabels = imageLabels,
                    mentionedGroupIds = mentionedGroupIds,
                    mml = mml,
                    pollId = pollId,
                    restrictedVisibility = restrictedVisibility.orEmpty(),
                ),
            )
        }.let(messageTransformer::transform)
    }

    private fun parseMentionedChannelMembers(reader: JsonReader): Map<String, MemberInfo>? {
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
            reader.skipValue()
            return null
        }

        reader.beginObject()
        val members = mutableMapOf<String, MemberInfo>()
        while (reader.hasNext()) {
            val userId = reader.nextName()
            parseMemberInfo(reader)?.let { members[userId] = it }
        }
        reader.endObject()

        return members
    }

    private fun parseMemberInfo(reader: JsonReader): MemberInfo? {
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
            reader.skipValue()
            return null
        }

        reader.beginObject()
        var channelRole: String? = null
        var notificationsMuted: Boolean? = null
        var custom: MutableMap<String, Any>? = null

        while (reader.hasNext()) {
            val key = reader.nextName()
            when (key) {
                "channel_role" -> channelRole = JsonParsingUtils.readNullableString(reader)
                "notifications_muted" -> notificationsMuted = JsonParsingUtils.readNullableBoolean(reader)
                // API v2 nests the member custom data under "custom", API v1 inlines it next to the declared
                // fields, so unknown keys are member custom data too.
                "custom" -> {
                    val nested = JsonParsingUtils.parseAnyMap(reader).orEmpty()
                    custom = (custom ?: mutableMapOf()).apply { putAll(nested) }
                }
                else -> custom = JsonParsingUtils.accumulateExtraData(key, reader, custom)
            }
        }
        reader.endObject()

        return MemberInfo(
            channelRole = channelRole,
            notificationsMuted = notificationsMuted ?: false,
            extraData = custom.orEmpty(),
        )
    }

    private companion object {
        /** The v2 keys, matched without allocating a key string, as the generated adapters match them. */
        private val V2_KEYS = JsonReader.Options.of(
            "attachments",
            "cid",
            "command",
            "created_at",
            "deleted_at",
            "html",
            "i18n",
            "id",
            "latest_reactions",
            "mentioned_users",
            "mentioned_here",
            "mentioned_channel",
            "mentioned_groups",
            "mentioned_group_ids",
            "mentioned_roles",
            "own_reactions",
            "parent_id",
            "pin_expires",
            "pinned",
            "pinned_at",
            "message_text_updated_at",
            "pinned_by",
            "quoted_message",
            "quoted_message_id",
            "reaction_counts",
            "reaction_scores",
            "reaction_groups",
            "reply_count",
            "deleted_reply_count",
            "shadowed",
            "show_in_channel",
            "silent",
            "text",
            "thread_participants",
            "type",
            "updated_at",
            "user",
            "moderation",
            "poll",
            "reminder",
            "shared_location",
            "member",
            "mentioned_channel_members",
            "deleted_for_me",
            "draft",
            "image_labels",
            "mml",
            "poll_id",
            "restricted_visibility",
            "custom",
        )
    }

    override fun toJson(p0: JsonWriter, p1: Message?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
