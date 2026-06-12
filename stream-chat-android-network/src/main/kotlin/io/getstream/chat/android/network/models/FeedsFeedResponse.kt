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
data class FeedsFeedResponse (
    @Json(name = "activity_count")
    val activityCount: kotlin.Int,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "description")
    val description: kotlin.String,

    @Json(name = "feed")
    val feed: kotlin.String,

    @Json(name = "follower_count")
    val followerCount: kotlin.Int,

    @Json(name = "following_count")
    val followingCount: kotlin.Int,

    @Json(name = "group_id")
    val groupId: kotlin.String,

    @Json(name = "id")
    val id: kotlin.String,

    @Json(name = "member_count")
    val memberCount: kotlin.Int,

    @Json(name = "name")
    val name: kotlin.String,

    @Json(name = "pin_count")
    val pinCount: kotlin.Int,

    @Json(name = "updated_at")
    val updatedAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "created_by")
    val createdBy: io.getstream.chat.android.network.models.UserResponse,

    @Json(name = "deleted_at")
    val deletedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "visibility")
    val visibility: kotlin.String? = null,

    @Json(name = "filter_tags")
    val filterTags: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "location")
    val location: io.getstream.chat.android.network.models.FeedsActivityLocation? = null
)
