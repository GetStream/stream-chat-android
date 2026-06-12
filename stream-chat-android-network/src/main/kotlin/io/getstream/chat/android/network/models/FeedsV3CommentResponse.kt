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
data class FeedsV3CommentResponse (
    @Json(name = "bookmark_count")
    val bookmarkCount: kotlin.Int,

    @Json(name = "confidence_score")
    val confidenceScore: kotlin.Float,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "downvote_count")
    val downvoteCount: kotlin.Int,

    @Json(name = "id")
    val id: kotlin.String,

    @Json(name = "object_id")
    val objectId: kotlin.String,

    @Json(name = "object_type")
    val objectType: kotlin.String,

    @Json(name = "reaction_count")
    val reactionCount: kotlin.Int,

    @Json(name = "reply_count")
    val replyCount: kotlin.Int,

    @Json(name = "score")
    val score: kotlin.Int,

    @Json(name = "status")
    val status: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "upvote_count")
    val upvoteCount: kotlin.Int,

    @Json(name = "mentioned_users")
    val mentionedUsers: kotlin.collections.List<io.getstream.chat.android.network.models.UserResponse> = emptyList(),

    @Json(name = "own_reactions")
    val ownReactions: kotlin.collections.List<io.getstream.chat.android.network.models.FeedsReactionResponse> = emptyList(),

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.UserResponse,

    @Json(name = "controversy_score")
    val controversyScore: kotlin.Float? = null,

    @Json(name = "deleted_at")
    val deletedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "edited_at")
    val editedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "parent_id")
    val parentId: kotlin.String? = null,

    @Json(name = "text")
    val text: kotlin.String? = null,

    @Json(name = "attachments")
    val attachments: kotlin.collections.List<io.getstream.chat.android.network.models.Attachment>? = emptyList(),

    @Json(name = "latest_reactions")
    val latestReactions: kotlin.collections.List<io.getstream.chat.android.network.models.FeedsReactionResponse>? = emptyList(),

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "moderation")
    val moderation: io.getstream.chat.android.network.models.ModerationV2Response? = null,

    @Json(name = "reaction_groups")
    val reactionGroups: kotlin.collections.Map<kotlin.String, io.getstream.chat.android.network.models.FeedsReactionGroupResponse>? = emptyMap()
)
