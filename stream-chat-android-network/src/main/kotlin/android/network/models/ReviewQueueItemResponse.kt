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

data class ReviewQueueItemResponse (
    @Json(name = "ai_text_severity")
    val aiTextSeverity: kotlin.String,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "entity_id")
    val entityId: kotlin.String,

    @Json(name = "entity_type")
    val entityType: kotlin.String,

    @Json(name = "escalated")
    val escalated: kotlin.Boolean,

    @Json(name = "flags_count")
    val flagsCount: kotlin.Int,

    @Json(name = "id")
    val id: kotlin.String,

    @Json(name = "latest_moderator_action")
    val latestModeratorAction: kotlin.String,

    @Json(name = "recommended_action")
    val recommendedAction: kotlin.String,

    @Json(name = "reviewed_by")
    val reviewedBy: kotlin.String,

    @Json(name = "severity")
    val severity: kotlin.Int,

    @Json(name = "status")
    val status: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "actions")
    val actions: kotlin.collections.List<io.getstream.chat.android.network.models.ActionLogResponse> = emptyList(),

    @Json(name = "bans")
    val bans: kotlin.collections.List<io.getstream.chat.android.network.models.BanInfoResponse> = emptyList(),

    @Json(name = "flags")
    val flags: kotlin.collections.List<io.getstream.chat.android.network.models.ModerationFlagResponse> = emptyList(),

    @Json(name = "languages")
    val languages: kotlin.collections.List<kotlin.String> = emptyList(),

    @Json(name = "completed_at")
    val completedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "config_key")
    val configKey: kotlin.String? = null,

    @Json(name = "entity_creator_id")
    val entityCreatorId: kotlin.String? = null,

    @Json(name = "escalated_at")
    val escalatedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "escalated_by")
    val escalatedBy: kotlin.String? = null,

    @Json(name = "reviewed_at")
    val reviewedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "teams")
    val teams: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "activity")
    val activity: io.getstream.chat.android.network.models.EnrichedActivity? = null,

    @Json(name = "appeal")
    val appeal: io.getstream.chat.android.network.models.AppealItemResponse? = null,

    @Json(name = "assigned_to")
    val assignedTo: io.getstream.chat.android.network.models.UserResponse? = null,

    @Json(name = "call")
    val call: io.getstream.chat.android.network.models.CallResponse? = null,

    @Json(name = "entity_creator")
    val entityCreator: io.getstream.chat.android.network.models.EntityCreatorResponse? = null,

    @Json(name = "escalation_metadata")
    val escalationMetadata: io.getstream.chat.android.network.models.EscalationMetadata? = null,

    @Json(name = "feeds_v2_activity")
    val feedsV2Activity: io.getstream.chat.android.network.models.EnrichedActivity? = null,

    @Json(name = "feeds_v2_reaction")
    val feedsV2Reaction: io.getstream.chat.android.network.models.Reaction? = null,

    @Json(name = "feeds_v3_activity")
    val feedsV3Activity: io.getstream.chat.android.network.models.FeedsV3ActivityResponse? = null,

    @Json(name = "feeds_v3_comment")
    val feedsV3Comment: io.getstream.chat.android.network.models.FeedsV3CommentResponse? = null,

    @Json(name = "message")
    val message: io.getstream.chat.android.network.models.ChatMessageResponse? = null,

    @Json(name = "moderation_payload")
    val moderationPayload: io.getstream.chat.android.network.models.ModerationPayloadResponse? = null,

    @Json(name = "reaction")
    val reaction: io.getstream.chat.android.network.models.Reaction? = null
)
