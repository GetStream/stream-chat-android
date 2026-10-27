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
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.models.User
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
 * Reads a message straight into its domain model, mapped by [mapping] as the generated path maps it: custom data under
 * `custom`, and the nested objects messages rarely carry read through their generated models.
 */
@Suppress("LongParameterList", "LargeClass")
internal class MessageAdapter(
    private val attachmentAdapter: JsonAdapter<Attachment>,
    private val reactionAdapter: JsonAdapter<Reaction>,
    private val reactionGroupAdapter: ReactionGroupAdapter,
    private val userAdapter: JsonAdapter<User>,
    private val dateAdapter: JsonAdapter<Date>,
    private val messageTransformer: MessageTransformer,
    private val mapping: DomainMapping,
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

    fun fromJson(reader: JsonReader, fallbackChannelInfo: ChannelInfo?): Message? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        return read(reader, fallbackChannelInfo)
    }

    /** Mirrors the generated path's `MessageResponse.toDomain`, so both build the same [Message]. */
    @Suppress("LongMethod", "CyclomaticComplexMethod", "ComplexMethod", "MagicNumber")
    private fun read(reader: JsonReader, fallbackChannelInfo: ChannelInfo?): Message {
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
            when (reader.selectName(KEYS)) {
                0 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_ARRAY, nullable = false)) {
                    attachments = JsonParsingUtils.parseList(reader, attachmentAdapter)
                }
                1 -> cid = reader.nextString()
                2 -> command = JsonParsingUtils.readNullableString(reader)
                3 -> createdAt = dateAdapter.fromJson(reader)
                4 -> deletedAt = dateAdapter.fromJson(reader)
                5 -> html = reader.nextString()
                6 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_OBJECT, nullable = true)) {
                    i18n = JsonParsingUtils.parseStringMap(reader)
                }
                7 -> id = reader.nextString()
                8 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_ARRAY, nullable = false)) {
                    latestReactions = JsonParsingUtils.parseList(reader, reactionAdapter)
                }
                9 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_ARRAY, nullable = false)) {
                    mentionedUsers = JsonParsingUtils.parseList(reader, userAdapter)
                }
                10 -> mentionedHere = reader.nextBoolean()
                11 -> mentionedChannel = reader.nextBoolean()
                12 -> mentionedGroups = mentionedGroupsAdapter.fromJson(reader)
                13 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_ARRAY, nullable = true)) {
                    mentionedGroupIds = JsonParsingUtils.parseStringList(reader)
                }
                14 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_ARRAY, nullable = true)) {
                    mentionedRoles = JsonParsingUtils.parseStringList(reader)
                }
                15 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_ARRAY, nullable = false)) {
                    ownReactions = JsonParsingUtils.parseList(reader, reactionAdapter)
                }
                16 -> parentId = JsonParsingUtils.readNullableString(reader)
                17 -> pinExpires = dateAdapter.fromJson(reader)
                18 -> pinned = reader.nextBoolean()
                19 -> pinnedAt = dateAdapter.fromJson(reader)
                20 -> messageTextUpdatedAt = dateAdapter.fromJson(reader)
                21 -> pinnedBy = userAdapter.fromJson(reader)
                22 -> quotedMessage = fromJson(reader, fallbackChannelInfo)
                23 -> quotedMessageId = JsonParsingUtils.readNullableString(reader)
                24 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_OBJECT, nullable = false)) {
                    reactionCounts = JsonParsingUtils.parseIntMap(reader)
                }
                25 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_OBJECT, nullable = false)) {
                    reactionScores = JsonParsingUtils.parseIntMap(reader)
                }
                26 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_OBJECT, nullable = true)) {
                    reactionGroups = reactionGroupAdapter.parseReactionGroupsMap(reader)
                }
                27 -> replyCount = reader.nextInt()
                28 -> deletedReplyCount = reader.nextInt()
                29 -> shadowed = reader.nextBoolean()
                30 -> showInChannel = JsonParsingUtils.readNullableBoolean(reader)
                31 -> silent = reader.nextBoolean()
                32 -> text = reader.nextString()
                33 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_ARRAY, nullable = true)) {
                    threadParticipants = JsonParsingUtils.parseList(reader, userAdapter)
                }
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
                48 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_ARRAY, nullable = false)) {
                    restrictedVisibility = JsonParsingUtils.parseStringList(reader)
                }
                49 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_OBJECT, nullable = false)) {
                    custom = JsonParsingUtils.readJsonObject(reader)
                }
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
                latestReactions = latestReactions.orEmpty().ofMessage(id),
                mentionedUsers = mentionedUsers.orEmpty(),
                mentionedHere = mentionedHere,
                mentionedChannel = mentionedChannel,
                mentionedGroups = mentionedGroups.orEmpty().map { it.toDomain() },
                mentionedRoles = mentionedRoles.orEmpty(),
                ownReactions = ownReactions.orEmpty().ofMessage(id),
                parentId = parentId,
                pinExpires = pinExpires,
                pinned = pinned,
                pinnedAt = pinnedAt,
                pinnedBy = pinnedBy,
                reactionCounts = reactionCounts.toMutable(),
                reactionScores = reactionScores.toMutable(),
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
                updatedAt = poll?.updatedAt?.takeIf { it.time > updatedAt.time } ?: updatedAt,
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

    // The backend can send reactions of other messages; the list is only copied when it does.
    private fun List<Reaction>.ofMessage(messageId: String): List<Reaction> =
        if (all { it.messageId == messageId }) this else filter { it.messageId == messageId }

    private fun Map<String, Int>?.toMutable(): MutableMap<String, Int> =
        this as? MutableMap ?: this.orEmpty().toMutableMap()

    private companion object {
        /** The keys, matched without allocating a key string, as the generated adapters match them. */
        private val KEYS = JsonReader.Options.of(
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
