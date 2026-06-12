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

data class Reaction (
    @Json(name = "activity_id")
    val activityId: kotlin.String,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "kind")
    val kind: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "user_id")
    val userId: kotlin.String,

    @Json(name = "deleted_at")
    val deletedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "id")
    val id: kotlin.String? = null,

    @Json(name = "parent")
    val parent: kotlin.String? = null,

    @Json(name = "score")
    val score: kotlin.Float? = null,

    @Json(name = "target_feeds")
    val targetFeeds: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "children_counts")
    val childrenCounts: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "data")
    val data: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "latest_children")
    val latestChildren: kotlin.collections.Map<kotlin.String, kotlin.collections.List<io.getstream.chat.android.network.models.Reaction>>? = emptyMap(),

    @Json(name = "moderation")
    val moderation: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "own_children")
    val ownChildren: kotlin.collections.Map<kotlin.String, kotlin.collections.List<io.getstream.chat.android.network.models.Reaction>>? = emptyMap(),

    @Json(name = "target_feeds_extra_data")
    val targetFeedsExtraData: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.User? = null
)
