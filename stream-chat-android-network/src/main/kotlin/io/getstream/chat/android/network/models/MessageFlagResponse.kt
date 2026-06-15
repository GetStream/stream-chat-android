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
data class MessageFlagResponse (
    @Json(name = "created_at")
    val createdAt: java.util.Date,

    @Json(name = "created_by_automod")
    val createdByAutomod: kotlin.Boolean,

    @Json(name = "updated_at")
    val updatedAt: java.util.Date,

    @Json(name = "approved_at")
    val approvedAt: java.util.Date? = null,

    @Json(name = "reason")
    val reason: kotlin.String? = null,

    @Json(name = "rejected_at")
    val rejectedAt: java.util.Date? = null,

    @Json(name = "reviewed_at")
    val reviewedAt: java.util.Date? = null,

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "details")
    val details: io.getstream.chat.android.network.models.FlagDetailsResponse? = null,

    @Json(name = "message")
    val message: io.getstream.chat.android.network.models.MessageResponse? = null,

    @Json(name = "moderation_feedback")
    val moderationFeedback: io.getstream.chat.android.network.models.FlagFeedbackResponse? = null,

    @Json(name = "moderation_result")
    val moderationResult: io.getstream.chat.android.network.models.MessageModerationResult? = null,

    @Json(name = "reviewed_by")
    val reviewedBy: io.getstream.chat.android.network.models.UserResponse? = null,

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.UserResponse? = null
)
