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
data class ModerationFlagResponse (
    @Json(name = "created_at")
    val createdAt: java.util.Date,

    @Json(name = "entity_id")
    val entityId: kotlin.String,

    @Json(name = "entity_type")
    val entityType: kotlin.String,

    @Json(name = "type")
    val type: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: java.util.Date,

    @Json(name = "user_id")
    val userId: kotlin.String,

    @Json(name = "result")
    val result: kotlin.collections.List<kotlin.collections.Map<kotlin.String, Any?>> = emptyList(),

    @Json(name = "entity_creator_id")
    val entityCreatorId: kotlin.String? = null,

    @Json(name = "reason")
    val reason: kotlin.String? = null,

    @Json(name = "review_queue_item_id")
    val reviewQueueItemId: kotlin.String? = null,

    @Json(name = "labels")
    val labels: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "moderation_payload")
    val moderationPayload: io.getstream.chat.android.network.models.ModerationPayloadResponse? = null,

    @Json(name = "review_queue_item")
    val reviewQueueItem: io.getstream.chat.android.network.models.ReviewQueueItemResponse? = null,

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.UserResponse? = null
)
