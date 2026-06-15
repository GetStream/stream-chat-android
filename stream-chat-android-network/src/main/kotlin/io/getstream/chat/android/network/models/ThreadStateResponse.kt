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
 * 
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ThreadStateResponse (
    @Json(name = "active_participant_count")
    val activeParticipantCount: kotlin.Int,

    @Json(name = "channel_cid")
    val channelCid: kotlin.String,

    @Json(name = "created_at")
    val createdAt: java.util.Date,

    @Json(name = "created_by_user_id")
    val createdByUserId: kotlin.String,

    @Json(name = "parent_message_id")
    val parentMessageId: kotlin.String,

    @Json(name = "participant_count")
    val participantCount: kotlin.Int,

    @Json(name = "title")
    val title: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: java.util.Date,

    @Json(name = "latest_replies")
    val latestReplies: kotlin.collections.List<io.getstream.chat.android.network.models.MessageResponse> = emptyList(),

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "deleted_at")
    val deletedAt: java.util.Date? = null,

    @Json(name = "last_message_at")
    val lastMessageAt: java.util.Date? = null,

    @Json(name = "reply_count")
    val replyCount: kotlin.Int? = null,

    @Json(name = "read")
    val read: kotlin.collections.List<io.getstream.chat.android.network.models.ReadStateResponse>? = emptyList(),

    @Json(name = "thread_participants")
    val threadParticipants: kotlin.collections.List<io.getstream.chat.android.network.models.ThreadParticipant>? = emptyList(),

    @Json(name = "channel")
    val channel: io.getstream.chat.android.network.models.ChannelResponse? = null,

    @Json(name = "created_by")
    val createdBy: io.getstream.chat.android.network.models.UserResponse? = null,

    @Json(name = "draft")
    val draft: io.getstream.chat.android.network.models.DraftResponse? = null,

    @Json(name = "parent_message")
    val parentMessage: io.getstream.chat.android.network.models.MessageResponse? = null
)
