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
 * Represents channel in chat
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ChannelResponse (
    @Json(name = "cid")
    val cid: kotlin.String,

    @Json(name = "created_at")
    val createdAt: java.util.Date,

    @Json(name = "disabled")
    val disabled: kotlin.Boolean,

    @Json(name = "frozen")
    val frozen: kotlin.Boolean,

    @Json(name = "id")
    val id: kotlin.String,

    @Json(name = "type")
    val type: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: java.util.Date,

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "auto_translation_enabled")
    val autoTranslationEnabled: kotlin.Boolean? = null,

    @Json(name = "auto_translation_language")
    val autoTranslationLanguage: kotlin.String? = null,

    @Json(name = "blocked")
    val blocked: kotlin.Boolean? = null,

    @Json(name = "cooldown")
    val cooldown: kotlin.Int? = null,

    @Json(name = "deleted_at")
    val deletedAt: java.util.Date? = null,

    @Json(name = "hidden")
    val hidden: kotlin.Boolean? = null,

    @Json(name = "hide_messages_before")
    val hideMessagesBefore: java.util.Date? = null,

    @Json(name = "last_message_at")
    val lastMessageAt: java.util.Date? = null,

    @Json(name = "member_count")
    val memberCount: kotlin.Int? = null,

    @Json(name = "message_count")
    val messageCount: kotlin.Int? = null,

    @Json(name = "mute_expires_at")
    val muteExpiresAt: java.util.Date? = null,

    @Json(name = "muted")
    val muted: kotlin.Boolean? = null,

    @Json(name = "team")
    val team: kotlin.String? = null,

    @Json(name = "truncated_at")
    val truncatedAt: java.util.Date? = null,

    @Json(name = "filter_tags")
    val filterTags: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "members")
    val members: kotlin.collections.List<io.getstream.chat.android.network.models.ChannelMemberResponse>? = emptyList(),

    @Json(name = "own_capabilities")
    val ownCapabilities: kotlin.collections.List<io.getstream.chat.android.network.models.ChannelOwnCapability>? = emptyList(),

    @Json(name = "config")
    val config: io.getstream.chat.android.network.models.ChannelConfigWithInfo? = null,

    @Json(name = "created_by")
    val createdBy: io.getstream.chat.android.network.models.UserResponse? = null,

    @Json(name = "truncated_by")
    val truncatedBy: io.getstream.chat.android.network.models.UserResponse? = null
)
