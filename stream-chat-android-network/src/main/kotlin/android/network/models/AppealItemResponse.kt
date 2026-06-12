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

data class AppealItemResponse (
    @Json(name = "appeal_reason")
    val appealReason: kotlin.String,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "entity_id")
    val entityId: kotlin.String,

    @Json(name = "entity_type")
    val entityType: kotlin.String,

    @Json(name = "id")
    val id: kotlin.String,

    @Json(name = "status")
    val status: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "ai_text_severity")
    val aiTextSeverity: kotlin.String? = null,

    @Json(name = "channel_cid")
    val channelCid: kotlin.String? = null,

    @Json(name = "config_key")
    val configKey: kotlin.String? = null,

    @Json(name = "decision_reason")
    val decisionReason: kotlin.String? = null,

    @Json(name = "recommended_action")
    val recommendedAction: kotlin.String? = null,

    @Json(name = "severity")
    val severity: kotlin.Int? = null,

    @Json(name = "actions")
    val actions: kotlin.collections.List<io.getstream.chat.android.network.models.ActionLogResponse>? = emptyList(),

    @Json(name = "attachments")
    val attachments: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "flag_labels")
    val flagLabels: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "flag_types")
    val flagTypes: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "flags")
    val flags: kotlin.collections.List<io.getstream.chat.android.network.models.ModerationFlagResponse>? = emptyList(),

    @Json(name = "entity_content")
    val entityContent: io.getstream.chat.android.network.models.ModerationPayload? = null,

    @Json(name = "moderation_action")
    val moderationAction: io.getstream.chat.android.network.models.ActionLogResponse? = null,

    @Json(name = "original_moderation_action")
    val originalModerationAction: io.getstream.chat.android.network.models.ActionLogResponse? = null,

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.UserResponse? = null
)
