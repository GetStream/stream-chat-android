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
data class ConfigResponse (
    @Json(name = "async")
    val async: kotlin.Boolean,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "key")
    val key: kotlin.String,

    @Json(name = "team")
    val team: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "supported_video_call_harm_types")
    val supportedVideoCallHarmTypes: kotlin.collections.List<kotlin.String> = emptyList(),

    @Json(name = "ai_image_label_definitions")
    val aiImageLabelDefinitions: kotlin.collections.List<io.getstream.chat.android.network.models.AIImageLabelDefinition>? = emptyList(),

    @Json(name = "available_bodyguard_profiles")
    val availableBodyguardProfiles: kotlin.collections.List<io.getstream.chat.android.network.models.BodyguardProfileSummary>? = emptyList(),

    @Json(name = "ai_image_config")
    val aiImageConfig: io.getstream.chat.android.network.models.AIImageConfig? = null,

    @Json(name = "ai_image_subclassifications")
    val aiImageSubclassifications: kotlin.collections.Map<kotlin.String, kotlin.collections.List<kotlin.String>>? = emptyMap(),

    @Json(name = "ai_text_config")
    val aiTextConfig: io.getstream.chat.android.network.models.AITextConfig? = null,

    @Json(name = "ai_video_config")
    val aiVideoConfig: io.getstream.chat.android.network.models.AIVideoConfig? = null,

    @Json(name = "automod_platform_circumvention_config")
    val automodPlatformCircumventionConfig: io.getstream.chat.android.network.models.AutomodPlatformCircumventionConfig? = null,

    @Json(name = "automod_semantic_filters_config")
    val automodSemanticFiltersConfig: io.getstream.chat.android.network.models.AutomodSemanticFiltersConfig? = null,

    @Json(name = "automod_toxicity_config")
    val automodToxicityConfig: io.getstream.chat.android.network.models.AutomodToxicityConfig? = null,

    @Json(name = "block_list_config")
    val blockListConfig: io.getstream.chat.android.network.models.BlockListConfig? = null,

    @Json(name = "flood_config")
    val floodConfig: io.getstream.chat.android.network.models.FloodConfig? = null,

    @Json(name = "llm_config")
    val llmConfig: io.getstream.chat.android.network.models.LLMConfig? = null,

    @Json(name = "velocity_filter_config")
    val velocityFilterConfig: io.getstream.chat.android.network.models.VelocityFilterConfig? = null,

    @Json(name = "video_call_rule_config")
    val videoCallRuleConfig: io.getstream.chat.android.network.models.VideoCallRuleConfig? = null
)
