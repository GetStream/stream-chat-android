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
 *
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ThreadStateResponse(
    @Json(name = "active_participant_count")
    internal val activeParticipantCount: Int,

    @Json(name = "channel_cid")
    internal val channelCid: String,

    @Json(name = "created_at")
    internal val createdAt: java.util.Date,

    @Json(name = "created_by_user_id")
    internal val createdByUserId: String,

    @Json(name = "parent_message_id")
    internal val parentMessageId: String,

    @Json(name = "participant_count")
    internal val participantCount: Int,

    @Json(name = "reply_count")
    internal val replyCount: Int,

    @Json(name = "title")
    internal val title: String,

    @Json(name = "updated_at")
    internal val updatedAt: java.util.Date,

    @Json(name = "latest_replies")
    internal val latestReplies: List<io.getstream.chat.android.network.models.MessageResponse> = emptyList(),

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "deleted_at")
    internal val deletedAt: java.util.Date? = null,

    @Json(name = "last_message_at")
    internal val lastMessageAt: java.util.Date? = null,

    @Json(name = "read")
    internal val read: List<io.getstream.chat.android.network.models.ReadStateResponse>? = null,

    @Json(name = "thread_participants")
    internal val threadParticipants: List<io.getstream.chat.android.network.models.ThreadParticipant>? = null,

    @Json(name = "channel")
    internal val channel: io.getstream.chat.android.network.models.ChannelResponse? = null,

    @Json(name = "created_by")
    internal val createdBy: io.getstream.chat.android.network.models.UserResponse? = null,

    @Json(name = "draft")
    internal val draft: io.getstream.chat.android.network.models.DraftResponse? = null,

    @Json(name = "parent_message")
    internal val parentMessage: io.getstream.chat.android.network.models.MessageResponse? = null,
)
