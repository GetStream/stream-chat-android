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

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.models

import com.squareup.moshi.Json

/**
 * Represents any chat message
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class MessageResponse(
    @Json(name = "cid")
    internal val cid: String,

    @Json(name = "created_at")
    internal val createdAt: java.util.Date,

    @Json(name = "deleted_reply_count")
    internal val deletedReplyCount: Int,

    @Json(name = "html")
    internal val html: String,

    @Json(name = "id")
    internal val id: String,

    @Json(name = "mentioned_channel")
    internal val mentionedChannel: Boolean,

    @Json(name = "mentioned_here")
    internal val mentionedHere: Boolean,

    @Json(name = "pinned")
    internal val pinned: Boolean,

    @Json(name = "reply_count")
    internal val replyCount: Int,

    @Json(name = "shadowed")
    internal val shadowed: Boolean,

    @Json(name = "silent")
    internal val silent: Boolean,

    @Json(name = "text")
    internal val text: String,

    @Json(name = "type")
    internal val type: String,

    @Json(name = "updated_at")
    internal val updatedAt: java.util.Date,

    @Json(name = "attachments")
    internal val attachments: List<io.getstream.chat.android.network.models.Attachment> = emptyList(),

    @Json(name = "latest_reactions")
    internal val latestReactions: List<io.getstream.chat.android.network.models.ReactionResponse> = emptyList(),

    @Json(name = "mentioned_users")
    internal val mentionedUsers: List<io.getstream.chat.android.network.models.UserResponse> = emptyList(),

    @Json(name = "own_reactions")
    internal val ownReactions: List<io.getstream.chat.android.network.models.ReactionResponse> = emptyList(),

    @Json(name = "restricted_visibility")
    internal val restrictedVisibility: List<String> = emptyList(),

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "reaction_counts")
    internal val reactionCounts: Map<String, Int> = emptyMap(),

    @Json(name = "reaction_scores")
    internal val reactionScores: Map<String, Int> = emptyMap(),

    @Json(name = "user")
    internal val user: io.getstream.chat.android.network.models.UserResponse,

    @Json(name = "command")
    internal val command: String? = null,

    @Json(name = "deleted_at")
    internal val deletedAt: java.util.Date? = null,

    @Json(name = "deleted_for_me")
    internal val deletedForMe: Boolean? = null,

    @Json(name = "message_text_updated_at")
    internal val messageTextUpdatedAt: java.util.Date? = null,

    @Json(name = "mml")
    internal val mml: String? = null,

    @Json(name = "parent_id")
    internal val parentId: String? = null,

    @Json(name = "pin_expires")
    internal val pinExpires: java.util.Date? = null,

    @Json(name = "pinned_at")
    internal val pinnedAt: java.util.Date? = null,

    @Json(name = "poll_id")
    internal val pollId: String? = null,

    @Json(name = "quoted_message_id")
    internal val quotedMessageId: String? = null,

    @Json(name = "show_in_channel")
    internal val showInChannel: Boolean? = null,

    @Json(name = "mentioned_group_ids")
    internal val mentionedGroupIds: List<String>? = emptyList(),

    @Json(name = "mentioned_groups")
    internal val mentionedGroups: List<io.getstream.chat.android.network.models.UserGroupResponse>? = emptyList(),

    @Json(name = "mentioned_roles")
    internal val mentionedRoles: List<String>? = emptyList(),

    @Json(name = "thread_participants")
    internal val threadParticipants: List<io.getstream.chat.android.network.models.UserResponse>? = emptyList(),

    @Json(name = "draft")
    internal val draft: io.getstream.chat.android.network.models.DraftResponse? = null,

    @Json(name = "i18n")
    internal val i18n: Map<String, String>? = emptyMap(),

    @Json(name = "image_labels")
    internal val imageLabels: Map<String, List<String>>? = emptyMap(),

    @Json(name = "member")
    internal val member: io.getstream.chat.android.network.models.ChannelMemberPartialResponse? = null,

    @Json(name = "moderation")
    internal val moderation: io.getstream.chat.android.network.models.ModerationV2Response? = null,

    @Json(name = "pinned_by")
    internal val pinnedBy: io.getstream.chat.android.network.models.UserResponse? = null,

    @Json(name = "poll")
    internal val poll: io.getstream.chat.android.network.models.PollResponseData? = null,

    @Json(name = "quoted_message")
    internal val quotedMessage: io.getstream.chat.android.network.models.MessageResponse? = null,

    @Json(name = "reaction_groups")
    internal val reactionGroups: Map<String, io.getstream.chat.android.network.models.ReactionGroupResponse>? = emptyMap(),

    @Json(name = "reminder")
    internal val reminder: io.getstream.chat.android.network.models.ReminderResponseData? = null,

    @Json(name = "shared_location")
    internal val sharedLocation: io.getstream.chat.android.network.models.SharedLocationResponseData? = null,
)
