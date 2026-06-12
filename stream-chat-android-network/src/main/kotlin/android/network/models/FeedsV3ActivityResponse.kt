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

data class FeedsV3ActivityResponse (
    @Json(name = "bookmark_count")
    val bookmarkCount: kotlin.Int,

    @Json(name = "comment_count")
    val commentCount: kotlin.Int,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "hidden")
    val hidden: kotlin.Boolean,

    @Json(name = "id")
    val id: kotlin.String,

    @Json(name = "popularity")
    val popularity: kotlin.Int,

    @Json(name = "preview")
    val preview: kotlin.Boolean,

    @Json(name = "reaction_count")
    val reactionCount: kotlin.Int,

    @Json(name = "restrict_replies")
    val restrictReplies: kotlin.String,

    @Json(name = "score")
    val score: kotlin.Float,

    @Json(name = "share_count")
    val shareCount: kotlin.Int,

    @Json(name = "type")
    val type: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "visibility")
    val visibility: kotlin.String,

    @Json(name = "attachments")
    val attachments: kotlin.collections.List<io.getstream.chat.android.network.models.Attachment> = emptyList(),

    @Json(name = "comments")
    val comments: kotlin.collections.List<io.getstream.chat.android.network.models.FeedsV3CommentResponse> = emptyList(),

    @Json(name = "feeds")
    val feeds: kotlin.collections.List<kotlin.String> = emptyList(),

    @Json(name = "filter_tags")
    val filterTags: kotlin.collections.List<kotlin.String> = emptyList(),

    @Json(name = "interest_tags")
    val interestTags: kotlin.collections.List<kotlin.String> = emptyList(),

    @Json(name = "latest_reactions")
    val latestReactions: kotlin.collections.List<io.getstream.chat.android.network.models.FeedsReactionResponse> = emptyList(),

    @Json(name = "mentioned_users")
    val mentionedUsers: kotlin.collections.List<io.getstream.chat.android.network.models.UserResponse> = emptyList(),

    @Json(name = "own_bookmarks")
    val ownBookmarks: kotlin.collections.List<io.getstream.chat.android.network.models.FeedsBookmarkResponse> = emptyList(),

    @Json(name = "own_reactions")
    val ownReactions: kotlin.collections.List<io.getstream.chat.android.network.models.FeedsReactionResponse> = emptyList(),

    @Json(name = "collections")
    val collections: kotlin.collections.Map<kotlin.String, io.getstream.chat.android.network.models.FeedsEnrichedCollectionResponse> = emptyMap(),

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "reaction_groups")
    val reactionGroups: kotlin.collections.Map<kotlin.String, io.getstream.chat.android.network.models.FeedsReactionGroupResponse> = emptyMap(),

    @Json(name = "search_data")
    val searchData: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.UserResponse,

    @Json(name = "deleted_at")
    val deletedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "edited_at")
    val editedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "expires_at")
    val expiresAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "friend_reaction_count")
    val friendReactionCount: kotlin.Int? = null,

    @Json(name = "is_read")
    val isRead: kotlin.Boolean? = null,

    @Json(name = "is_seen")
    val isSeen: kotlin.Boolean? = null,

    @Json(name = "is_watched")
    val isWatched: kotlin.Boolean? = null,

    @Json(name = "moderation_action")
    val moderationAction: kotlin.String? = null,

    @Json(name = "selector_source")
    val selectorSource: kotlin.String? = null,

    @Json(name = "text")
    val text: kotlin.String? = null,

    @Json(name = "visibility_tag")
    val visibilityTag: kotlin.String? = null,

    @Json(name = "friend_reactions")
    val friendReactions: kotlin.collections.List<io.getstream.chat.android.network.models.FeedsReactionResponse>? = emptyList(),

    @Json(name = "current_feed")
    val currentFeed: io.getstream.chat.android.network.models.FeedsFeedResponse? = null,

    @Json(name = "location")
    val location: io.getstream.chat.android.network.models.FeedsActivityLocation? = null,

    @Json(name = "metrics")
    val metrics: kotlin.collections.Map<kotlin.String, kotlin.Int>? = emptyMap(),

    @Json(name = "moderation")
    val moderation: io.getstream.chat.android.network.models.ModerationV2Response? = null,

    @Json(name = "notification_context")
    val notificationContext: io.getstream.chat.android.network.models.FeedsNotificationContext? = null,

    @Json(name = "parent")
    val parent: io.getstream.chat.android.network.models.FeedsV3ActivityResponse? = null,

    @Json(name = "poll")
    val poll: io.getstream.chat.android.network.models.PollResponseData? = null,

    @Json(name = "score_vars")
    val scoreVars: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap()
)
