/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-video-android/blob/main/LICENSE
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
    "UnusedImport"
)

package io.getstream.chat.android.network.models

import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.*
import kotlin.io.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Represents any chat message
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class MessageResponse (
    @Json(name = "cid")
    val cid: kotlin.String,

    @Json(name = "created_at")
    val createdAt: java.util.Date,

    @Json(name = "deleted_reply_count")
    val deletedReplyCount: kotlin.Int,

    @Json(name = "html")
    val html: kotlin.String,

    @Json(name = "id")
    val id: kotlin.String,

    @Json(name = "mentioned_channel")
    val mentionedChannel: kotlin.Boolean,

    @Json(name = "mentioned_here")
    val mentionedHere: kotlin.Boolean,

    @Json(name = "pinned")
    val pinned: kotlin.Boolean,

    @Json(name = "reply_count")
    val replyCount: kotlin.Int,

    @Json(name = "shadowed")
    val shadowed: kotlin.Boolean,

    @Json(name = "silent")
    val silent: kotlin.Boolean,

    @Json(name = "text")
    val text: kotlin.String,

    @Json(name = "type")
    val type: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: java.util.Date,

    @Json(name = "attachments")
    val attachments: kotlin.collections.List<io.getstream.chat.android.network.models.Attachment> = emptyList(),

    @Json(name = "latest_reactions")
    val latestReactions: kotlin.collections.List<io.getstream.chat.android.network.models.ReactionResponse> = emptyList(),

    @Json(name = "mentioned_users")
    val mentionedUsers: kotlin.collections.List<io.getstream.chat.android.network.models.UserResponse> = emptyList(),

    @Json(name = "own_reactions")
    val ownReactions: kotlin.collections.List<io.getstream.chat.android.network.models.ReactionResponse> = emptyList(),

    @Json(name = "restricted_visibility")
    val restrictedVisibility: kotlin.collections.List<kotlin.String> = emptyList(),

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "reaction_counts")
    val reactionCounts: kotlin.collections.Map<kotlin.String, kotlin.Int> = emptyMap(),

    @Json(name = "reaction_scores")
    val reactionScores: kotlin.collections.Map<kotlin.String, kotlin.Int> = emptyMap(),

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.UserResponse,

    @Json(name = "command")
    val command: kotlin.String? = null,

    @Json(name = "deleted_at")
    val deletedAt: java.util.Date? = null,

    @Json(name = "deleted_for_me")
    val deletedForMe: kotlin.Boolean? = null,

    @Json(name = "message_text_updated_at")
    val messageTextUpdatedAt: java.util.Date? = null,

    @Json(name = "mml")
    val mml: kotlin.String? = null,

    @Json(name = "parent_id")
    val parentId: kotlin.String? = null,

    @Json(name = "pin_expires")
    val pinExpires: java.util.Date? = null,

    @Json(name = "pinned_at")
    val pinnedAt: java.util.Date? = null,

    @Json(name = "poll_id")
    val pollId: kotlin.String? = null,

    @Json(name = "quoted_message_id")
    val quotedMessageId: kotlin.String? = null,

    @Json(name = "show_in_channel")
    val showInChannel: kotlin.Boolean? = null,

    @Json(name = "mentioned_group_ids")
    val mentionedGroupIds: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "mentioned_groups")
    val mentionedGroups: kotlin.collections.List<io.getstream.chat.android.network.models.UserGroupResponse>? = emptyList(),

    @Json(name = "mentioned_roles")
    val mentionedRoles: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "thread_participants")
    val threadParticipants: kotlin.collections.List<io.getstream.chat.android.network.models.UserResponse>? = emptyList(),

    @Json(name = "draft")
    val draft: io.getstream.chat.android.network.models.DraftResponse? = null,

    @Json(name = "i18n")
    val i18n: kotlin.collections.Map<kotlin.String, kotlin.String>? = emptyMap(),

    @Json(name = "image_labels")
    val imageLabels: kotlin.collections.Map<kotlin.String, kotlin.collections.List<kotlin.String>>? = emptyMap(),

    @Json(name = "member")
    val member: io.getstream.chat.android.network.models.ChannelMemberResponse? = null,

    @Json(name = "moderation")
    val moderation: io.getstream.chat.android.network.models.ModerationV2Response? = null,

    @Json(name = "pinned_by")
    val pinnedBy: io.getstream.chat.android.network.models.UserResponse? = null,

    @Json(name = "poll")
    val poll: io.getstream.chat.android.network.models.PollResponseData? = null,

    @Json(name = "quoted_message")
    val quotedMessage: io.getstream.chat.android.network.models.MessageResponse? = null,

    @Json(name = "reaction_groups")
    val reactionGroups: kotlin.collections.Map<kotlin.String, io.getstream.chat.android.network.models.ReactionGroupResponse>? = emptyMap(),

    @Json(name = "reminder")
    val reminder: io.getstream.chat.android.network.models.ReminderResponseData? = null,

    @Json(name = "shared_location")
    val sharedLocation: io.getstream.chat.android.network.models.SharedLocationResponseData? = null
)
