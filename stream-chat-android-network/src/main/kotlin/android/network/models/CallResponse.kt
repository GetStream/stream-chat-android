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

data class CallResponse (
    @Json(name = "backstage")
    val backstage: kotlin.Boolean,

    @Json(name = "captioning")
    val captioning: kotlin.Boolean,

    @Json(name = "cid")
    val cid: kotlin.String,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "current_session_id")
    val currentSessionId: kotlin.String,

    @Json(name = "id")
    val id: kotlin.String,

    @Json(name = "recording")
    val recording: kotlin.Boolean,

    @Json(name = "transcribing")
    val transcribing: kotlin.Boolean,

    @Json(name = "translating")
    val translating: kotlin.Boolean,

    @Json(name = "type")
    val type: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "blocked_user_ids")
    val blockedUserIds: kotlin.collections.List<kotlin.String> = emptyList(),

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "channel_cid")
    val channelCid: kotlin.String? = null,

    @Json(name = "ended_at")
    val endedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "join_ahead_time_seconds")
    val joinAheadTimeSeconds: kotlin.Int? = null,

    @Json(name = "routing_number")
    val routingNumber: kotlin.String? = null,

    @Json(name = "starts_at")
    val startsAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "team")
    val team: kotlin.String? = null,

    @Json(name = "created_by")
    val createdBy: io.getstream.chat.android.network.models.UserResponse? = null
)
