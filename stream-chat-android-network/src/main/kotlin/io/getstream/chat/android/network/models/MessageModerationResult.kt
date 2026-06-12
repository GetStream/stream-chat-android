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
 * Result of the message moderation
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class MessageModerationResult (
    @Json(name = "action")
    val action: kotlin.String,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "message_id")
    val messageId: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "user_bad_karma")
    val userBadKarma: kotlin.Boolean,

    @Json(name = "user_karma")
    val userKarma: kotlin.Float,

    @Json(name = "blocked_word")
    val blockedWord: kotlin.String? = null,

    @Json(name = "blocklist_name")
    val blocklistName: kotlin.String? = null,

    @Json(name = "moderated_by")
    val moderatedBy: kotlin.String? = null,

    @Json(name = "ai_moderation_response")
    val aiModerationResponse: io.getstream.chat.android.network.models.ModerationResponse? = null,

    @Json(name = "moderation_thresholds")
    val moderationThresholds: io.getstream.chat.android.network.models.Thresholds? = null
)
