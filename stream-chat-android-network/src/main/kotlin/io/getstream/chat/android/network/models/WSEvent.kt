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
 * Represents an BaseEvent that happened in Stream Chat
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class WSEvent (
    @Json(name = "created_at")
    val createdAt: java.util.Date,

    @Json(name = "type")
    val type: kotlin.String,

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "automoderation")
    val automoderation: kotlin.Boolean? = null,

    @Json(name = "channel_id")
    val channelId: kotlin.String? = null,

    @Json(name = "channel_last_message_at")
    val channelLastMessageAt: java.util.Date? = null,

    @Json(name = "channel_type")
    val channelType: kotlin.String? = null,

    @Json(name = "cid")
    val cid: kotlin.String? = null,

    @Json(name = "connection_id")
    val connectionId: kotlin.String? = null,

    @Json(name = "parent_id")
    val parentId: kotlin.String? = null,

    @Json(name = "reason")
    val reason: kotlin.String? = null,

    @Json(name = "team")
    val team: kotlin.String? = null,

    @Json(name = "thread_id")
    val threadId: kotlin.String? = null,

    @Json(name = "user_id")
    val userId: kotlin.String? = null,

    @Json(name = "watcher_count")
    val watcherCount: kotlin.Int? = null,

    @Json(name = "automoderation_scores")
    val automoderationScores: io.getstream.chat.android.network.models.ModerationResponse? = null,

    @Json(name = "channel")
    val channel: io.getstream.chat.android.network.models.ChannelResponse? = null,

    @Json(name = "created_by")
    val createdBy: io.getstream.chat.android.network.models.UserResponse? = null,

    @Json(name = "me")
    val me: io.getstream.chat.android.network.models.OwnUserResponse? = null,

    @Json(name = "member")
    val member: io.getstream.chat.android.network.models.ChannelMemberResponse? = null,

    @Json(name = "message")
    val message: io.getstream.chat.android.network.models.MessageResponse? = null,

    @Json(name = "message_update")
    val messageUpdate: io.getstream.chat.android.network.models.MessageUpdate? = null,

    @Json(name = "poll")
    val poll: io.getstream.chat.android.network.models.PollResponseData? = null,

    @Json(name = "poll_vote")
    val pollVote: io.getstream.chat.android.network.models.PollVoteResponseData? = null,

    @Json(name = "reaction")
    val reaction: io.getstream.chat.android.network.models.ReactionResponse? = null,

    @Json(name = "thread")
    val thread: io.getstream.chat.android.network.models.ThreadResponse? = null,

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.UserResponse? = null
)
